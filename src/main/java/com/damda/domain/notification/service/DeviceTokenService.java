package com.damda.domain.notification.service;

import com.damda.domain.notification.model.DeviceTokenReq;

import java.util.UUID;

public interface DeviceTokenService {
    void register(UUID memberId, DeviceTokenReq req);
    void unregister(UUID memberId, String fcmToken);
}
