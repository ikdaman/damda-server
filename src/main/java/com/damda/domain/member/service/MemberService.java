package com.damda.domain.member.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.model.MemberRes;

/**
 * 회원 서비스
 */
public interface MemberService {
    Boolean isNicknameAvailable(String nickname);

    void withdrawMember(Member member);

    MemberRes getMember(Member member);

    Boolean isNicknameValid(String nickname);
}
