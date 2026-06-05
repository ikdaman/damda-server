package com.damda.domain.notification.repository;

import com.damda.domain.notification.entity.NotificationLog;
import com.damda.domain.notification.entity.NotificationLog.Status;
import com.damda.domain.notification.entity.NotificationLog.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Query("""
        select count(n)
        from NotificationLog n
        where n.memberId = :memberId
          and n.type = :type
          and n.status = :status
          and n.sentAt >= :from
          and n.sentAt < :to
    """)
    long countByMemberAndTypeAndStatusAndSentAtBetween(UUID memberId, Type type, Status status,
                                                       LocalDateTime from, LocalDateTime to);

    default boolean existsSuccessInRange(UUID memberId, Type type, LocalDateTime from, LocalDateTime to) {
        return countByMemberAndTypeAndStatusAndSentAtBetween(memberId, type, Status.SUCCESS, from, to) > 0;
    }
}
