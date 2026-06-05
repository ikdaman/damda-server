package com.damda.domain.notification.service;

import com.damda.domain.notification.entity.DeviceToken;
import com.damda.domain.notification.model.DeviceTokenReq;
import com.damda.domain.notification.repository.DeviceTokenRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    @Transactional
    public void register(UUID memberId, DeviceTokenReq req) {
        deviceTokenRepository.findByFcmToken(req.getFcmToken())
                .ifPresentOrElse(existing -> {
                    if (!existing.getMemberId().equals(memberId)) {
                        existing.changeOwner(memberId);
                    }
                    existing.touch();
                }, () -> {
                    deviceTokenRepository.save(
                            DeviceToken.builder()
                                    .memberId(memberId)
                                    .fcmToken(req.getFcmToken())
                                    .platform(req.getPlatform())
                                    .build()
                    );
                });
    }

    @Override
    @Transactional
    public void unregister(UUID memberId, String fcmToken) {
        deviceTokenRepository.deleteByFcmToken(fcmToken);
    }
}
