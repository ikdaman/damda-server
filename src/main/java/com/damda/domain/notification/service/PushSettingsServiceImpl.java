package com.damda.domain.notification.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.domain.notification.model.PushSettingsReq;
import com.damda.domain.notification.model.PushSettingsRes;
import com.damda.global.exception.BaseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.damda.global.exception.ErrorCode.NOT_FOUND_USER; // 프로젝트 실제 코드에 맞게 조정

@Service
@Slf4j
@RequiredArgsConstructor
public class PushSettingsServiceImpl implements PushSettingsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public PushSettingsRes getSettings(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        return new PushSettingsRes(member.isPushEnabled());
    }

    @Override
    @Transactional
    public PushSettingsRes updateSettings(UUID memberId, PushSettingsReq req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        member.updatePushEnabled(req.isPushEnabled());

        return new PushSettingsRes(member.isPushEnabled());
    }
}
