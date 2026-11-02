package com.damda.domain.collection.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션에 책 담기 응답 DTO
 * - slotNo       : 책이 들어간 칸 (맨 앞의 빈 칸)
 * - savedToShelf : 이번 요청으로 서가에 새로 저장되었는지 여부
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionBookRes {
    private Long collectionId;
    private Long mybookId;
    private int slotNo;
    private boolean savedToShelf;

    @Builder
    public CollectionBookRes(Long collectionId, Long mybookId, int slotNo, boolean savedToShelf) {
        this.collectionId = collectionId;
        this.mybookId = mybookId;
        this.slotNo = slotNo;
        this.savedToShelf = savedToShelf;
    }
}
