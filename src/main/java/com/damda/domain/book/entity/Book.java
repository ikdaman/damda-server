package com.damda.domain.book.entity;

import com.damda.global.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 도서 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@DynamicUpdate
public class Book extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 13)
    private String isbn;

    @Column(name = "total_page")
    @Builder.Default
    private Integer totalPage = 0;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "aladin_id")
    private String aladinId;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Author> author;

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updatePublisher(String publisher) {
        this.publisher = publisher;
    }

    public void updatePublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public void updateIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void updateTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    @Builder
    public Book(Long bookId, String title, String publisher,
                LocalDateTime publishDate, String description, String isbn,
                int totalPage, String coverImage, String aladinId, List<Author> author) {
        this.bookId = bookId;
        this.title = title;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.description = description;
        this.isbn = isbn;
        this.totalPage = totalPage;
        this.coverImage = coverImage;
        this.aladinId = aladinId;
        this.author = author;
    }
}