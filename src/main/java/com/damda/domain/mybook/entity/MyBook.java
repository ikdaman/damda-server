package com.damda.domain.mybook.entity;

import com.damda.domain.book.entity.Book;
import com.damda.domain.member.entity.Member;
import com.damda.global.common.BaseTime;
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
public class MyBook extends BaseTime {
    @Id
    @GeneratedValue
    @Column(name = "mybook_id", nullable = false, updatable = false)
    private Long mybookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status", nullable = false)
    @Builder.Default
    private ReadingStatus readingStatus = ReadingStatus.TODO;

    @Column(name = "started_date")
    private LocalDateTime startedDate;

    @Column(name = "finished_date")
    private LocalDateTime finishedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MyBook.Status status = MyBook.Status.ACTIVE; // 기본값 ACTIVE

    // 독서 상태 ENUM
    public enum ReadingStatus {
        TODO,       // 읽을 예정
        INPROGRESS, // 읽는 중
        DONE        // 완독
    }

    // 계정 상태 ENUM
    public enum Status {
        ACTIVE, INACTIVE
    }

    public void updateReason(String reason) {
        this.reason = reason;
    }

    public void updateStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }

    public void updateFinishedDate(LocalDateTime finishedDate) {
        this.finishedDate = finishedDate;
    }

    public void updateReadingStatus() {
        if(this.startedDate == null) {
            this.readingStatus = ReadingStatus.TODO;
        } else if(this.finishedDate == null) {
            this.readingStatus = ReadingStatus.INPROGRESS;
        } else {
            this.readingStatus = ReadingStatus.DONE;
        }
    }

    public void updateToInactive() {
        this.status = Status.INACTIVE;
    }

    @Builder
    public MyBook(Long mybookId, Member member, Book book, String reason,
                  ReadingStatus readingStatus, LocalDateTime startedDate,
                  LocalDateTime finishedDate, Status status) {
        this.mybookId = mybookId;
        this.member = member;
        this.book = book;
        this.reason = reason;
        this.readingStatus = readingStatus;
        this.startedDate = startedDate;
        this.finishedDate = finishedDate;
        this.status = status;
    }
}