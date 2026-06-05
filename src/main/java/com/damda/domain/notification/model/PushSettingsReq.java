package com.damda.domain.notification.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSettingsReq {

    private boolean pushEnabled;

    public PushSettingsReq(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }
}
