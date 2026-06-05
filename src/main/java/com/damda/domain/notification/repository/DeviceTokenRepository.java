package com.damda.domain.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.damda.domain.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByFcmToken(String fcmToken);
    List<DeviceToken> findAllByMemberId(UUID memberId);
    void deleteByFcmToken(String fcmToken);
}
