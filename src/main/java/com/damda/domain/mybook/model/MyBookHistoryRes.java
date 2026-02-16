package com.damda.domain.mybook.model;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 내 책 히스토리 - BookItem 정의
 */
@Getter
@Builder
public class MyBookHistoryRes {

    @Getter
    @Builder
    public static class BookItem {
        private Long mybookId;
        private LocalDateTime startedDate;
        private LocalDateTime finishedDate;
        private BookInfo bookInfo;
    }

    @Getter
    @Builder
    public static class BookInfo {
        private String title;
        private List<String> author;
        private String coverImage;
        private String description;
    }
}
