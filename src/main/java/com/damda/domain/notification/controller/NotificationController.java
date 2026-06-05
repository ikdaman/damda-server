package com.damda.domain.notification.controller;

import com.damda.domain.notification.model.DeleteDeviceTokenReq;
import com.damda.domain.notification.model.DeviceTokenReq;
import com.damda.domain.notification.model.PushSettingsReq;
import com.damda.domain.notification.model.PushSettingsRes;
import com.damda.domain.notification.model.SendPushRes;
import com.damda.domain.notification.model.TestPushReq;
import com.damda.domain.notification.service.DeviceTokenService;
import com.damda.domain.notification.service.NotificationService;
import com.damda.domain.notification.service.PushSettingsService;
import com.damda.global.auth.model.AuthMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final DeviceTokenService deviceTokenService;
    private final PushSettingsService pushSettingsService;
    private final NotificationService notificationService;

    /**
     * 디바이스 토큰 등록/갱신
     * POST /notifications/device-token
     */
    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(
            @RequestBody @Valid DeviceTokenReq req,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        UUID memberId = authMember.getMember().getMemberId();
        deviceTokenService.register(memberId, req);
        return ResponseEntity.ok().build();
    }

    /**
     * 디바이스 토큰 해제
     * DELETE /notifications/device-token
     */
    @DeleteMapping("/device-token")
    public ResponseEntity<Void> deleteDeviceToken(
            @RequestBody @Valid DeleteDeviceTokenReq req,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        UUID memberId = authMember.getMember().getMemberId();
        deviceTokenService.unregister(memberId, req.getFcmToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * 푸시 설정 조회
     * GET /notifications/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<PushSettingsRes> getSettings(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        UUID memberId = authMember.getMember().getMemberId();
        return ResponseEntity.ok(pushSettingsService.getSettings(memberId));
    }

    /**
     * 푸시 설정 변경
     * PATCH /notifications/settings
     */
    @PatchMapping("/settings")
    @Transactional
    public ResponseEntity<PushSettingsRes> updateSettings(
            @RequestBody @Valid PushSettingsReq req,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        UUID memberId = authMember.getMember().getMemberId();
        return ResponseEntity.ok(pushSettingsService.updateSettings(memberId, req));
    }

    /**
     * (P0 전용) 테스트 푸시 발송
     * 운영에서는 비노출 권장 → dev 프로필에서만 열기
     */
    @Profile("dev")
    @PostMapping("/test")
    public ResponseEntity<?> testPush(
            @RequestBody @Valid TestPushReq req,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        UUID memberId = authMember.getMember().getMemberId();

        // 토큰 조회/전송/결과까지 서비스에서 처리 (컨트롤러는 orchestration만)
        SendPushRes res = notificationService.sendTestPush(memberId, req);
        return ResponseEntity.ok(res);
    }
}
