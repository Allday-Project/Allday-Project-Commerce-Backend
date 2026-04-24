package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
                        chatRoom.title,
                        chatRoom.chatRoomStatus,
                        chatRoom.lastMessageAt,
                        chatRoom.createdAt
                ))
                .from(chatRoom)
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
    public List<ChatRoom> findInactiveRooms(LocalDateTime cutoff) {
        return queryFactory
                .selectFrom(chatRoom)
                .where(
                        chatRoom.chatRoomStatus.in(
                                ChatRoomStatus.WAITING,
                                ChatRoomStatus.IN_PROGRESS
                        ),
                        chatRoom.lastMessageAt.before(cutoff)
                ).fetch();
    }

    private BooleanExpression statusEq(ChatRoomStatus status) {
        return status != null ? chatRoom.chatRoomStatus.eq(status) : null;
    }
}
