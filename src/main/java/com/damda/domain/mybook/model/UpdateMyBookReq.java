package com.damda.domain.mybook.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.openapitools.jackson.nullable.JsonNullable;

/**
 * 나의 책 수정 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateMyBookReq {
    @NotBlank
    @Pattern(regexp = "^(STORE|HISTORY)$", message = "shelfType은 STORE 또는 HISTORY이어야 합니다.")
    private String shelfType;
    @Size(max = 500, message = "추가 이유는 500자를 초과할 수 없습니다.")
    private JsonNullable<String> reason = JsonNullable.undefined();
    private JsonNullable<UpdateBookInfo> bookInfo = JsonNullable.undefined();
    private JsonNullable<UpdateHistoryInfo> historyInfo = JsonNullable.undefined();

    @Getter
    public static class UpdateBookInfo {
        private JsonNullable<String> title = JsonNullable.undefined();
        private JsonNullable<String> author = JsonNullable.undefined();
        private JsonNullable<String> publisher = JsonNullable.undefined();
        private JsonNullable<String> publishDate = JsonNullable.undefined();
        @Size(min = 13, max = 13, message = "ISBN은 13자여야 합니다.")
        private JsonNullable<String> isbn = JsonNullable.undefined();
        private JsonNullable<Integer> totalPage = JsonNullable.undefined();
    }

    @Getter
    public static class UpdateHistoryInfo {
        private JsonNullable<String> startedDate = JsonNullable.undefined();
        private JsonNullable<String> finishedDate = JsonNullable.undefined();
    }
}
