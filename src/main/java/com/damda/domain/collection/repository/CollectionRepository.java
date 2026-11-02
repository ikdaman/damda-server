package com.damda.domain.collection.repository;

import com.damda.domain.collection.entity.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 컬렉션 Repository
 */
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    /**
     * 나의 컬렉션 전체 목록 (만든 순서)
     */
    @Query("SELECT c FROM Collection c " +
           "WHERE c.member.memberId = :memberId " +
           "ORDER BY c.createdAt ASC, c.collectionId ASC")
    List<Collection> findAllByMemberId(@Param("memberId") UUID memberId);

    /**
     * 내 컬렉션 조회 (소유자 확인 포함)
     */
    @Query("SELECT c FROM Collection c " +
           "WHERE c.collectionId = :collectionId " +
           "AND c.member.memberId = :memberId")
    Optional<Collection> findByCollectionIdAndMemberId(
            @Param("collectionId") Long collectionId,
            @Param("memberId") UUID memberId
    );

    /**
     * 내 컬렉션 조회 + 행 잠금
     * - 책 담기/빼기가 동시에 들어와도 같은 칸에 두 권이 들어가지 않도록 컬렉션 단위로 직렬화한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Collection c " +
           "WHERE c.collectionId = :collectionId " +
           "AND c.member.memberId = :memberId")
    Optional<Collection> findByCollectionIdAndMemberIdForUpdate(
            @Param("collectionId") Long collectionId,
            @Param("memberId") UUID memberId
    );
}
