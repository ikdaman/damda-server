package com.damda.domain.collection.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션에 담기 전 확인 응답 DTO
 * - onShelf             : 이미 서가에 있는 책인지 (true 면 저장 시트 없이 mybookId 로 바로 담기)
 * - mybookId            : 서가에 있는 책의 나의 책 ID (없으면 null)
 * - alreadyInCollection : 이 컬렉션에 이미 담긴 책인지 (true 면 "이미 이 컬렉션에 담긴 책이에요" 안내)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionBookCheckRes {
    private boolean onShelf;
    private Long mybookId;
    private boolean alreadyInCollection;

    @Builder
    public CollectionBookCheckRes(boolean onShelf, Long mybookId, boolean alreadyInCollection) {
        this.onShelf = onShelf;
        this.mybookId = mybookId;
        this.alreadyInCollection = alreadyInCollection;
    }
}
