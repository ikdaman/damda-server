package com.damda.domain.mybook.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 독서 이력 정보 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryInfo {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime startedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime finishedDate;

    @Builder
    public HistoryInfo(LocalDateTime startedDate, LocalDateTime finishedDate) {
        this.startedDate = startedDate;
        this.finishedDate = finishedDate;
    }
}