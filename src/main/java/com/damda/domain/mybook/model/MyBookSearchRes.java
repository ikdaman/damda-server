package com.damda.domain.mybook.model;

import com.damda.domain.mybook.entity.MyBook.ReadingStatus;
import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 내 책 통합 검색 - BookItem 정의
 */
@Getter
@Builder
public class MyBookSearchRes {

    @Getter
    @Builder
    public static class BookItem {
        private Long mybookId;
        private ReadingStatus readingStatus;
        private LocalDateTime createdDate;
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
