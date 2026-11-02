package com.damda.domain.collection.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 컬렉션 상세(주제 서가의 한 쪽) 응답 DTO (정책서 2-1)
 * - slots 는 항상 9개(1~9번 칸)이며, 책이 없는 칸은 empty=true 이다.
 * - 컬렉션의 권수는 내려주지 않는다. (정책서: 권수 표시 안 함)
 * - 책 띠지(reason)는 읽고 싶은 이유가 있는 책에만 값이 있다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionDetailRes {

    private Long collectionId;
    private String name;
    private LocalDateTime createdAt;
    private List<Slot> slots;

    @Builder
    public CollectionDetailRes(Long collectionId, String name, LocalDateTime createdAt, List<Slot> slots) {
        this.collectionId = collectionId;
        this.name = name;
        this.createdAt = createdAt;
        this.slots = slots;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Slot {
        private int slotNo;
        private boolean empty;
        private Long mybookId;
        private String reason;
        private BookInfo bookInfo;

        @Builder
        public Slot(int slotNo, boolean empty, Long mybookId, String reason, BookInfo bookInfo) {
            this.slotNo = slotNo;
            this.empty = empty;
            this.mybookId = mybookId;
            this.reason = reason;
            this.bookInfo = bookInfo;
        }

        public static Slot empty(int slotNo) {
            return Slot.builder()
                    .slotNo(slotNo)
                    .empty(true)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BookInfo {
        private String title;
        private List<String> author;
        private String coverImage;

        @Builder
        public BookInfo(String title, List<String> author, String coverImage) {
            this.title = title;
            this.author = author;
            this.coverImage = coverImage;
        }
    }
}
