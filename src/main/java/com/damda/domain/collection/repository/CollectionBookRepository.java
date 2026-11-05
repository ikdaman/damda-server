package com.damda.domain.collection.repository;

import com.damda.domain.collection.entity.Collection;
import com.damda.domain.collection.entity.CollectionBook;
import com.damda.domain.mybook.entity.MyBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * 컬렉션에 담긴 책 Repository
 */
public interface CollectionBookRepository extends JpaRepository<CollectionBook, Long> {

    /**
     * 컬렉션 상세: 담긴 책 (Book, Author, Writer 를 join fetch 로 한 번에 조회)
     * - 서가에서 삭제된(INACTIVE) 책은 제외
     */
    @Query("SELECT cb FROM CollectionBook cb " +
           "JOIN FETCH cb.mybook mb " +
           "JOIN FETCH mb.book b " +
           "LEFT JOIN FETCH b.author a " +
           "LEFT JOIN FETCH a.writer w " +
           "WHERE cb.collection = :collection " +
           "AND mb.status = 'ACTIVE' " +
           "ORDER BY cb.slotNo ASC")
    List<CollectionBook> findAllWithBookByCollection(@Param("collection") Collection collection);

    /**
     * 컬렉션 목록: 내 모든 컬렉션에 담긴 책 (대표 표지 계산용, 한 번에 조회해 N+1 방지)
     */
    @Query("SELECT cb FROM CollectionBook cb " +
           "JOIN FETCH cb.collection c " +
           "JOIN FETCH cb.mybook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE c.member.memberId = :memberId " +
           "AND mb.status = 'ACTIVE' " +
           "ORDER BY cb.slotNo ASC")
    List<CollectionBook> findAllWithBookByMemberId(@Param("memberId") UUID memberId);

    boolean existsByCollectionAndMybook(Collection collection, MyBook mybook);

    /**
     * 컬렉션에 현재 저장된 연결 (수정 시 최종 목록과 비교용)
     */
    @Query("SELECT cb FROM CollectionBook cb " +
           "JOIN FETCH cb.mybook mb " +
           "WHERE cb.collection = :collection")
    List<CollectionBook> findAllByCollection(@Param("collection") Collection collection);

    /**
     * 서가에서 삭제된(INACTIVE) 책이 컬렉션에 남긴 연결 정리
     * - 나의 책 삭제는 soft delete 이므로, 컬렉션에 담긴 채로 남은 칸이 계속 차지되지 않게 한다.
     */
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CollectionBook cb " +
           "WHERE cb.collection = :collection " +
           "AND EXISTS (SELECT 1 FROM MyBook mb " +
           "            WHERE mb.mybookId = cb.mybook.mybookId AND mb.status = 'INACTIVE')")
    int deleteInactiveBooks(@Param("collection") Collection collection);

    /**
     * 내 서가의 책 일괄 조회 (나의 책 ID, ACTIVE, 내 책만) - 요청의 mybookId 항목 확인용
     */
    @Query("SELECT mb FROM MyBook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE mb.mybookId IN :mybookIds " +
           "AND mb.member.memberId = :memberId " +
           "AND mb.status = 'ACTIVE'")
    List<MyBook> findShelfBooksByIds(
            @Param("memberId") UUID memberId,
            @Param("mybookIds") List<Long> mybookIds
    );

    /**
     * 내 서가의 책 일괄 조회 (알라딘 ID, ACTIVE) - 요청의 bookInfo 항목이 이미 서가에 있는지 확인용
     * - 같은 책은 서가에 하나만 둘 수 있다. (정책서 3-7)
     * - 나의 책 쪽에 중복이 남아 있어도 깨지지 않도록 호출하는 쪽에서 가장 먼저 저장한 책을 쓴다.
     */
    @Query("SELECT mb FROM MyBook mb " +
           "JOIN FETCH mb.book b " +
           "WHERE mb.member.memberId = :memberId " +
           "AND b.aladinId IN :aladinIds " +
           "AND mb.status = 'ACTIVE' " +
           "ORDER BY mb.createdAt ASC")
    List<MyBook> findShelfBooksByMemberIdAndAladinIds(
            @Param("memberId") UUID memberId,
            @Param("aladinIds") List<String> aladinIds
    );

    /**
     * 서가에 이미 있는 책 찾기 (같은 알라딘 ID, ACTIVE)
     * - 같은 책은 서가에 하나만 둘 수 있다. (정책서 3-7)
     * - 나의 책 쪽에 중복이 남아 있어도 깨지지 않도록 List 로 받아 가장 먼저 저장한 책을 쓴다.
     */
    @Query("SELECT mb FROM MyBook mb " +
            "JOIN FETCH mb.book b " +
            "WHERE mb.member.memberId = :memberId " +
            "AND b.aladinId = :aladinId " +
            "AND mb.status = 'ACTIVE' " +
            "ORDER BY mb.createdAt ASC")
    List<MyBook> findShelfBooksByMemberIdAndAladinId(
            @Param("memberId") UUID memberId,
            @Param("aladinId") String aladinId
    );
}
