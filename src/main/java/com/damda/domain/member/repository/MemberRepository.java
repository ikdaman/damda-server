package com.damda.domain.member.repository;

import com.damda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 회원 레퍼지토리
 */
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByProviderAndProviderIdAndStatus(
            Member.Provider provider,
            String providerId,
            Member.Status status
    );
    Optional<Member> findByMemberIdAndStatus(UUID memberId, Member.Status status);
    Boolean existsByNicknameAndStatus(String nickname, Member.Status status);
    Optional<Member> findByNicknameAndStatusIs(String nickname, Member.Status status);
}
