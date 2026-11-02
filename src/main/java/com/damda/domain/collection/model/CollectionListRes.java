package com.damda.domain.collection.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 나의 컬렉션 목록 응답 DTO (정책서 2-3)
 * - 목록 상단에 컬렉션 개수가 보인다. (totalCount)
 * - 각 항목에는 대표 표지 2장과 컬렉션 이름이 보인다.
 *   대표 표지는 컬렉션에 담긴 앞의 두 권이고, 책이 없는 컬렉션은 빈 배열이다.
 * - 컬렉션은 만든 순서(오래된 것 → 최신)로 정렬된다. (주제 서가 쪽 순서와 동일)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionListRes {

    private int totalCount;
    private List<CollectionItem> collections;

    @Builder
    public CollectionListRes(int totalCount, List<CollectionItem> collections) {
        this.totalCount = totalCount;
        this.collections = collections;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CollectionItem {
        private Long collectionId;
        private String name;
        private List<RepresentativeBook> representativeBooks;
        private LocalDateTime createdAt;

        @Builder
        public CollectionItem(Long collectionId, String name,
                              List<RepresentativeBook> representativeBooks, LocalDateTime createdAt) {
            this.collectionId = collectionId;
            this.name = name;
            this.representativeBooks = representativeBooks;
            this.createdAt = createdAt;
        }
    }

    /**
     * 대표 표지 (coverImage 가 null 이면 클라이언트에서 단색(#D4D4D4) 블록으로 표시)
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RepresentativeBook {
        private Long mybookId;
        private String coverImage;

        @Builder
        public RepresentativeBook(Long mybookId, String coverImage) {
            this.mybookId = mybookId;
            this.coverImage = coverImage;
        }
    }
}
