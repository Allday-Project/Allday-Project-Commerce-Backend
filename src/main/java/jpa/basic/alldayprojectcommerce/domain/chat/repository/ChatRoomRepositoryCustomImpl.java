package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static jpa.basic.alldayprojectcommerce.domain.chat.entity.QChatRoom.chatRoom;
import static jpa.basic.alldayprojectcommerce.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    /**
     * 전체 채팅방 조회 - QueryDSL Offset 페이징
     *
     * 특정 페이지로 바로 이동하려는 관리자 UX와
     * 채팅방 데이터는 읽기 빈도가 낮아 Offset 성능 부담 허용 범위
     */
    @Override
    public Page<ChatRoomResponse> findAllWithFilter(ChatRoomStatus status, Pageable pageable) {
        List<ChatRoomResponse> content = queryFactory
                .select(Projections.constructor(
                        ChatRoomResponse.class,
                        chatRoom.id,
                        chatRoom.userId,
                        user.email,
                        chatRoom.title,
                        chatRoom.chatRoomStatus,
                        chatRoom.lastMessageAt,
                        chatRoom.createdAt
                ))
                .from(chatRoom)
                .leftJoin(user).on(user.id.eq(chatRoom.userId))
                .where(statusEq(status))
                .orderBy(chatRoom.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리를 분리해서 JOIN 비용 줄이기
        Long count = queryFactory
                .select(chatRoom.count())
                .from(chatRoom)
                .where(statusEq(status))
                .fetchOne();

        return new PageImpl<>(content, pageable, count == null ? 0L : count);
    }

    @Override
    public List<ChatRoom> findInactiveRooms(LocalDateTime cutoff, Long lastId, int batchSize) {
        return queryFactory
                .selectFrom(chatRoom)
                .where(
                        gtRoomId(lastId),
                        chatRoom.chatRoomStatus.in(
                                ChatRoomStatus.WAITING,
                                ChatRoomStatus.IN_PROGRESS
                        ),
                        chatRoom.lastMessageAt.before(cutoff)
                )
                .orderBy(chatRoom.id.asc())  // ID 오름차순 - 커서 기반 페이징
                .limit(batchSize)
                .fetch();
    }

    @Override
    public void bulkCompleteRooms(List<Long> roomIds) {
        queryFactory
                .update(chatRoom)
                .set(chatRoom.chatRoomStatus, ChatRoomStatus.COMPLETED)
                .setNull(chatRoom.activeFlag)
                .where(
                        chatRoom.id.in(roomIds),
                        chatRoom.chatRoomStatus.in(
                                ChatRoomStatus.WAITING,
                                ChatRoomStatus.IN_PROGRESS
                        )
                )
                .execute();

        /**
         * bulk update를 진행하면서 1차 캐시에 남아있는 데이터를 초기화해서
         * 다음 조회 시 DB에서 최신 데이터를 가져오도록 강제
         */
        em.clear();
    }

    private BooleanExpression statusEq(ChatRoomStatus status) {
        return status != null ? chatRoom.chatRoomStatus.eq(status) : null;
    }

    private BooleanExpression gtRoomId(Long lastId) {
        return lastId != null ? chatRoom.id.gt(lastId) : null;
    }
}
