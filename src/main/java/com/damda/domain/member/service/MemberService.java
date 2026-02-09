package com.damda.domain.member.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.model.MemberReq;
import com.damda.domain.member.model.MemberRes;

import java.util.UUID;

/**
 * 회원 서비스
 */
public interface MemberService {
    Boolean isNicknameAvailable(String nickname);

    void withdrawMember(Member member);

    MemberRes getMember(Member member);

    MemberRes updateMember(Member member, MemberReq memberReq);
}
