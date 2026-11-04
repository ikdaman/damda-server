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
     * 컬렉션 만들기: 이름 + 최종 책 목록을 한 번에 저장 (정책서 2-4, 2-5)
     */
    CollectionDetailRes createCollection(UUID memberId, CollectionReq req);

    /*
     * 컬렉션 수정: 이름 + 최종 책 목록으로 전체 교체 (정책서 2-5)
     */
    CollectionDetailRes updateCollection(UUID memberId, Long collectionId, UpdateCollectionReq req);

    /*
     * 컬렉션에 담기 전 확인: 서가에 있는 책인지 / 이미 이 컬렉션에 담긴 책인지 (정책서 2-5)
     */
    CollectionBookCheckRes checkBook(UUID memberId, Long collectionId, Integer aladinId);
}
