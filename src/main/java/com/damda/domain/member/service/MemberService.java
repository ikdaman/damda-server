package com.damda.domain.member.service;

import com.damda.domain.member.entity.Member;

/**
 * 회원 서비스
 */
public interface MemberService {
    Boolean isNicknameAvailable(String nickname);
    void withdrawMember(Member member);
}
