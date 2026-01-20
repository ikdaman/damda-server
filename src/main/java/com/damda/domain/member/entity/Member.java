package com.damda.domain.member.entity;

import com.damda.global.common.BaseTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.util.UUID;

/**
 * 회원 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@DynamicUpdate
public class Member extends BaseTime {
    @Id
    @GeneratedValue
    @Column(name = "member_id", nullable = false, updatable = false, columnDefinition = "UUID DEFAULT uuid_generate_v7()")
    private UUID memberId;

    @Column(length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    @Column(name = "provider_id", unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE; // 기본값 ACTIVE

    // 소셜 로그인 제공자 ENUM
    public enum Provider {
        KAKAO, NAVER, GOOGLE, APPLE
    }

    // 계정 상태 ENUM
    public enum Status {
        ACTIVE, INACTIVE
    }

    @Builder
    public Member(UUID memberId, String nickname, Provider provider, String providerId, Status status) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.status = status;
    }

    public void updateNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateProviderId(String providerId) {
        this.providerId = providerId;
    }
}
