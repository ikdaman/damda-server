package com.damda.domain.notification.service;

import com.damda.domain.notification.model.SendPushRes;
import com.damda.domain.notification.model.TestPushReq;

import java.util.UUID;

public interface NotificationService {

    String randomBodyA();

    String randomBodyB(String bookTitle, int daysAgo);

    void sendPushAToMember(UUID memberId);

    void sendPushBToMember(UUID memberId);

    SendPushRes sendTestPush(UUID memberId, TestPushReq req);
}
