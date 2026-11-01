package com.damda.domain.mybook.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Long myBookId;
    private String title;
    private String author;
    private String coverImage;
    private String publisher;
    private String aladinId;
    private Long totalRecords;
    private String reason;
    private String readingStatus;
    private String createdDate;
}
