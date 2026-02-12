package com.damda.global.auth.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NaverUserRes {
    private String resultcode;
    private String message;
    private NaverUserResDetail response;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaverUserResDetail {
        private String id;
        private String name;
    }
}
