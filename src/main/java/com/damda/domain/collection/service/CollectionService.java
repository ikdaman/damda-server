package com.damda.domain.collection.service;

import com.damda.domain.collection.model.*;

import java.util.UUID;

/**
 * 컬렉션 서비스
 */
public interface CollectionService {

    /*
     * 나의 컬렉션 목록 조회 (정책서 2-3)
     */
    CollectionListRes getMyCollections(UUID memberId);

    /*
     * 컬렉션 상세(주제 서가 한 쪽) 조회 (정책서 2-1)
     */
    CollectionDetailRes getCollectionDetail(UUID memberId, Long collectionId);

    /*
     * 컬렉션 만들기 (정책서 2-4)
     */
    CollectionRes createCollection(UUID memberId, CollectionReq req);

    /*
     * 컬렉션 이름 수정 (정책서 2-5)
     */
    CollectionRes updateCollection(UUID memberId, Long collectionId, UpdateCollectionReq req);

    /*
     * 컬렉션에 담기 전 확인: 서가에 있는 책인지 / 이미 이 컬렉션에 담긴 책인지 (정책서 2-5)
     */
    CollectionBookCheckRes checkBook(UUID memberId, Long collectionId, Integer aladinId);

    /*
     * 컬렉션에 책 담기 (정책서 2-5)
     */
    CollectionBookRes addBook(UUID memberId, Long collectionId, CollectionBookReq req);

    /*
     * 컬렉션에서 책 빼기 (정책서 2-5)
     */
    void removeBook(UUID memberId, Long collectionId, Long mybookId);
}
