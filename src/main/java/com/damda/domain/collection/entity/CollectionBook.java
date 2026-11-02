package com.damda.domain.collection.entity;

import com.damda.domain.mybook.entity.MyBook;
import com.damda.global.common.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션에 담긴 책 (컬렉션 - 나의 책 연결)
 *
 * - 같은 컬렉션에는 같은 책을 한 번만 담을 수 있다. (uk_collection_book_mybook)
 * - 같은 칸에는 한 권만 들어간다. (uk_collection_book_slot)
 * - 같은 책을 여러 컬렉션에 담을 수 있다.
 * - 책을 빼면 그 칸만 비고, 다른 책의 칸 번호는 바뀌지 않는다. (정책서 2-5 책 빼기)
 *   그래서 순서가 아니라 칸 번호(slotNo, 1~9)를 직접 저장한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "collection_book",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_collection_book_mybook", columnNames = {"collection_id", "mybook_id"}),
                @UniqueConstraint(name = "uk_collection_book_slot", columnNames = {"collection_id", "slot_no"})
        }
)
public class CollectionBook extends BaseTime {

    @Id
    @GeneratedValue
    @Column(name = "collection_book_id", nullable = false, updatable = false)
    private Long collectionBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mybook_id", nullable = false)
    private MyBook mybook;

    /** 칸 번호 1~9 (왼쪽 위 → 오른쪽, 위 → 아래 순서) */
    @Column(name = "slot_no", nullable = false)
    private Integer slotNo;

    @Builder
    public CollectionBook(Long collectionBookId, Collection collection, MyBook mybook, Integer slotNo) {
        this.collectionBookId = collectionBookId;
        this.collection = collection;
        this.mybook = mybook;
        this.slotNo = slotNo;
    }
}
