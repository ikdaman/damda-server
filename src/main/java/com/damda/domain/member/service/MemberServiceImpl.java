package com.damda.domain.member.service;

import com.damda.domain.auth.service.AuthService;
import com.damda.domain.member.entity.Member;
import com.damda.domain.member.model.MemberRes;
import com.damda.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public Boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNicknameAndStatus(nickname, Member.Status.ACTIVE);
    }

    @Override
    @Transactional
    public void withdrawMember(Member member) {
        authService.logout(member.getMemberId());
        member.updateStatus(Member.Status.INACTIVE);
        member.updateProviderId(null);
        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberRes getMember(Member member) {
        // TODO: if member == null ~~ 임시 코드, 차후 삭제
        if(member == null){
            member = memberRepository.findByNicknameAndStatusIs("민니", Member.Status.ACTIVE).orElse(null);
        }

        MemberRes response = MemberRes.builder()
                .nickname(member.getNickname())
                .build();

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isNicknameValid(String nickname) {
        Member member = memberRepository.findByNicknameAndStatusIs(nickname, Member.Status.ACTIVE).orElse(null);

        return member == null ? true : false;
    }
}
