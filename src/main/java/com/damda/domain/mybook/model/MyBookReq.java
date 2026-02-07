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

    /**
     * 책 정보 DTO
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BookInfo {

        @NotBlank(message = "책 출처는 필수입니다.")
        @Pattern(regexp = "^(ALADIN|CUSTOM)$", message = "출처는 ALADIN 또는 CUSTOM이어야 합니다.")
        private String source;

        private Integer aladinId;

        @Pattern(
                regexp = "^(?:97[89])?\\d{9}[\\dXx]$",
                message = "유효한 ISBN 형식이 아닙니다."
        )
        private String isbn;

        @NotBlank(message = "책 제목은 필수입니다.")
        private String title;

        @NotBlank(message = "저자는 필수입니다.")
        private String author;

        @NotBlank(message = "출판사는 필수입니다.")
        private String publisher;

        private String description;

        @NotNull(message = "총 페이지 수는 필수입니다.")
        private Integer totalPage;

        @NotNull(message = "출판일은 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDateTime publishDate;

        private String coverImage;

        @Builder
        public BookInfo(String source, Integer aladinId, String isbn, String title,
                        String author, String publisher, String description,
                        Integer totalPage, LocalDateTime publishDate, String coverImage) {
            this.source = source;
            this.aladinId = aladinId;
            this.isbn = isbn;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.description = description;
            this.totalPage = totalPage;
            this.publishDate = publishDate;
            this.coverImage = coverImage;
        }
    }

    /**
     * 독서 이력 정보 DTO
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class HistoryInfo {

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
}
