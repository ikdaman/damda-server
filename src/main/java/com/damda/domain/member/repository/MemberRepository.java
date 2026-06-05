package com.damda.domain.member.repository;

import com.damda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.lastBookActionAt = :now WHERE m.memberId = :memberId")
    void updateLastBookActionAt(@Param("memberId") UUID memberId, @Param("now") LocalDateTime now);
    @Query("""
    select m from Member m
        where m.pushEnabled = true
          and (m.lastBookActionAt is null or m.lastBookActionAt <= :threshold)
          and m.status = 'ACTIVE'
    """)
    List<Member> findCandidatesForPushA(@Param("threshold") LocalDateTime threshold);
    @Query("""
        select m from Member m
        where m.pushEnabled = true
          and m.status = 'ACTIVE'
    """)
    List<Member> findAllPushEnabledActiveMembers();
}
