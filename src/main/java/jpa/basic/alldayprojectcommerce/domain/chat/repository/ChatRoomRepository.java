package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jakarta.persistence.LockModeType;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {

    /**
     * 유저의 활성 채팅방 조회
     *
     * activeFlag = 1 방은 유저당 최대 1개
     */
    Optional<ChatRoom> findByUserIdAndActiveFlag(Long userId, Integer activeFlag);

    /**
     * 관리자 전용
     *
     * 고객이 종료하는 동시에 관리자가 참여하는 Race Condition이 발생할 수 있음
     * -> 비관적 락을 사용해서 정합성을 보장
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT cr
           FROM ChatRoom cr
           WHERE cr.id = :roomId
           """)
    Optional<ChatRoom> findByIdForUpdate(@Param("roomId") Long roomId);


}
