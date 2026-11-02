package com.damda.domain.mybook.model;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 서가 조회 - BookItem 정의
 */
@Getter
@Builder
public class MyBookStorePageRes {

    @Getter
    @Builder
    public static class BookItem {
        private Long mybookId;
        private LocalDateTime createdDate;
        private String reason;
        private BookInfo bookInfo;
    }

    @Getter
    @Builder
    public static class BookInfo {
        private String title;
        private List<String> author;
        private String coverImage;
        private String description;
        // 책등 너비 계산용 페이지 수
        private Integer totalPage;
    }
}
