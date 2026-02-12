package com.damda.domain.mybook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 나의 책 정보 조회 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBookDetailRes {
    private BookInfo bookInfo;
    private HistoryInfo historyInfo;
    private String mybookId;
    private String readingStatus;
    private String shelfType;
    private String createdDate;
    private String reason;

    /**
     * 책 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private String bookId;
        private String source;
        private String title;
        private String author;
        private String coverImage;
        private String publisher;
        private int totalPage;
        private String publishDate;
        private String isbn;
        private String aladinId;
    }

    /**
     * 독서 이력 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryInfo {
        private String startedDate;
        private String finishedDate;
    }
}
