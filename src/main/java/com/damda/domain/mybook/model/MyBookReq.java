package com.damda.domain.mybook.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 나의 책 추가 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBookReq {

    @NotNull(message = "책 정보는 필수입니다.")
    @Valid
    private BookInfo bookInfo;

    @Valid
    private HistoryInfo historyInfo;

    @Size(max = 500, message = "추가 이유는 500자를 초과할 수 없습니다.")
    private String reason;

    @Builder
    public MyBookReq(BookInfo bookInfo, HistoryInfo historyInfo, String reason) {
        this.bookInfo = bookInfo;
        this.historyInfo = historyInfo;
        this.reason = reason;
    }

}
