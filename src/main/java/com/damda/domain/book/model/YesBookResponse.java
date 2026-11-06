package com.damda.domain.book.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YesBookResponse(
        Boolean success,
        String message,
        String errorCode,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            Integer currentPage,
            Integer pageSize,
            Integer totalCount,
            List<Item> items
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            Integer itemId,        // 책 고유 ID
            String title,        // 책 제목
            String author,       // 저자
            String publisher,    // 출판사
            Integer pages,       // 페이지 수 (nullable)
            String cover,        // 책 표지 이미지 URL
            String publishDate   // 출간일
    ) {}
}
