package com.damda.domain.collection.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 컬렉션 만들기 / 이름 수정 응답 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionRes {
    private Long collectionId;
    private String name;
    private LocalDateTime createdAt;

    @Builder
    public CollectionRes(Long collectionId, String name, LocalDateTime createdAt) {
        this.collectionId = collectionId;
        this.name = name;
        this.createdAt = createdAt;
    }
}
