package com.damda.global.auth.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.global.auth.model.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberIdAndStatus(UUID.fromString(username), Member.Status.ACTIVE).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return new AuthMember(member);
    }
}
