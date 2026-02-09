package com.damda.domain.mybook.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * 나의 책 수정 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMyBookReq {
    @NotBlank
    @Pattern(regexp = "^(STORE|HISTORY)$", message = "shelfType은 STORE 또는 HISTORY이어야 합니다.")
    private String shelfType;
    @Max(value = 500, message = "추가 이유는 500자를 초과할 수 없습니다.")
    private Optional<String> reason;
    private Optional<UpdateBookInfo> bookInfo;
    private Optional<UpdateHistoryInfo> historyInfo;

    @Getter
    public static class UpdateBookInfo {
        private Optional<String> title;
        private Optional<String> author;
        private Optional<String> publisher;
        private Optional<String> publishDate;
        @Size(min = 13, max = 13, message = "ISBN은 13자여야 합니다.")
        private Optional<String> isbn;
        private Optional<Integer> totalPage;
    }

    @Getter
    public static class UpdateHistoryInfo {
        private Optional<String> startedDate;
        private Optional<String> finishedDate;
    }
}
