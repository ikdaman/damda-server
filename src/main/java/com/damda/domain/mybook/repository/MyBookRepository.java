package com.damda.domain.mybook.repository;

import com.damda.domain.book.entity.Book;
import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.member.entity.Member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {
    boolean existsByMemberAndBookAndStatus(Member member, Book Book, MyBook.Status active);

    @EntityGraph(attributePaths = {"book"})
    Optional<MyBook> findByMybookIdAndStatusIs(Long mybookId, MyBook.Status status);

    @Query("SELECT mb FROM MyBook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE mb.member = :member " +
           "AND mb.readingStatus = :readingStatus " +
           "AND (:keyword IS NULL OR b.title LIKE %:keyword%)")
    Page<MyBook> findAllByMemberAndReadingStatusAndKeyword(
        @Param("member") Member member,
        @Param("readingStatus") MyBook.ReadingStatus readingStatus,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("SELECT mb FROM MyBook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE mb.member = :member " +
           "AND mb.readingStatus IN :readingStatuses " +
           "AND (:keyword IS NULL OR b.title LIKE %:keyword%)")
    Page<MyBook> findAllByMemberAndReadingStatusesAndKeyword(
        @Param("member") Member member,
        @Param("readingStatuses") java.util.List<MyBook.ReadingStatus> readingStatuses,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * 내 책 통합 검색 (내 서점 + 히스토리)
     * 정렬: 1순위 책 제목 정확도, 2순위 최신 등록순
     */
    @Query("SELECT mb FROM MyBook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE mb.member = :member " +
           "AND mb.status = 'ACTIVE' " +
           "AND b.title LIKE %:query% " +
           "ORDER BY " +
           "CASE WHEN b.title = :query THEN 0 " +
           "     WHEN b.title LIKE CONCAT(:query, '%') THEN 1 " +
           "     ELSE 2 END ASC, " +
           "mb.createdAt DESC")
    Page<MyBook> searchByQuery(
        @Param("member") Member member,
        @Param("query") String query,
        Pageable pageable
    );
}
