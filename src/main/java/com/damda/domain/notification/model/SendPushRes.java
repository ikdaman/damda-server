package com.damda.domain.notification.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SendPushRes {

    private boolean anySuccess;

    public SendPushRes(boolean anySuccess) {
        this.anySuccess = anySuccess;
    }

    public boolean isAnySuccess() {
        return anySuccess;
    }

    public void setAnySuccess(boolean anySuccess) {
        this.anySuccess = anySuccess;
    }

    public static SendPushRes empty() {
        return new SendPushRes(false);
    }
}
