package com.damda.domain.mybook.entity;

import com.damda.global.common.BaseTime;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@DynamicUpdate
public class Record extends BaseTime {

    @Id
    @GeneratedValue
    @Column(name = "record_id", nullable = false, updatable = false)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mybook_id", nullable = false)
    private MyBook myBook;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType recordType;

    public enum RecordType {
        INPROGRESS, TODO, DONE,
        BUY, LEND, GIFT, EBOOK, SENTENCE, TALK, REMIND, PRESENT, BORROW, REREAD,
        FREE
    }

    @Builder
    public Record(Long recordId, MyBook myBook, String memo, RecordType recordType) {
        this.recordId = recordId;
        this.myBook = myBook;
        this.memo = memo;
        this.recordType = recordType;
    }
}
