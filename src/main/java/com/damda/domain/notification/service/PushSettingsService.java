package com.damda.domain.notification.service;

import com.damda.domain.notification.model.PushSettingsReq;
import com.damda.domain.notification.model.PushSettingsRes;

import java.util.UUID;

public interface PushSettingsService {
    PushSettingsRes getSettings(UUID memberId);
    PushSettingsRes updateSettings(UUID memberId, PushSettingsReq req);
}
