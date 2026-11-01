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

import java.time.LocalDateTime;

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
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    public enum RecordType {
        INPROGRESS, TODO, DONE,
        BUY, LEND, GIFT, EBOOK,
        SENTENCE, TALK, REMIND, PRESENT, BORROW, REREAD,
        FREE;

        public enum READING_STATUS {
            TODO, INPROGRESS, DONE
        }

        public enum GET_STATUS {
            BUY, LEND, GIFT, EBOOK
        }

        public enum MOMENT_STATUS {
            SENTENCE, TALK, REMIND, PRESENT, BORROW, REREAD
        }

        public static boolean isReadingStatus(RecordType recordType) {
            return recordType == INPROGRESS || recordType == TODO || recordType == DONE;
        }
    }

    @Builder
    public Record(Long recordId, MyBook myBook, String memo, RecordType recordType, LocalDateTime recordDate) {
        this.recordId = recordId;
        this.myBook = myBook;
        this.memo = memo;
        this.recordType = recordType;
        this.recordDate = recordDate;
    }
}
