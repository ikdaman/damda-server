package com.damda.domain.notification.model;

import com.damda.domain.notification.enumerate.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceTokenReq {

    @NotBlank
    private String fcmToken;

    @NotNull
    private Platform platform;

    public DeviceTokenReq(String fcmToken, Platform platform) {
        this.fcmToken = fcmToken;
        this.platform = platform;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
