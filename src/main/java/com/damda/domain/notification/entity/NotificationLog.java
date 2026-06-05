package com.damda.domain.notification.entity;

import com.damda.global.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@DynamicUpdate
public class NotificationLog extends BaseTime {

    public enum Type { A, B }
    public enum Status { SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 1)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.SUCCESS;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder
    public NotificationLog(Long id, UUID memberId, Type type, Status status, LocalDateTime sentAt) {
        this.id = id;
        this.memberId = memberId;
        this.type = type;
        this.status = status;
        this.sentAt = sentAt;
    }

    @PrePersist
    void prePersist() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Status.SUCCESS;
        }
    }
}
