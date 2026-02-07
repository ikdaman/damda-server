package com.damda.domain.mybook.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyBookRes {
    private Integer mybookId;
    private String title;
    private String writer;
    private Integer itemId;
    private String coverImage;
    private String reason;
    private String createdAt;

    @Builder
    public MyBookRes(int mybookId, String title, String writer, Integer itemId,
                     String coverImage, String reason, String createdAt) {
        this.mybookId = mybookId;
        this.title = title;
        this.writer = writer;
        this.itemId = itemId;
        this.coverImage = coverImage;
        this.reason = reason;
        this.createdAt = createdAt;
    }
}
