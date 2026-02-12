package com.damda.domain.mybook.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.mybook.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 나의 책 서비스
 */
public interface MyBookService {

    MyBookDetailRes getMyBookDetail(UUID memberId, Long mybookId);
    MyBookRes addMyBook(UUID memberId, MyBookReq dto);

    void deleteMyBook(UUID memberId, Integer mybookId);

    Long updateMyBook(Member member, Long mybookId, UpdateMyBookReq myBookReq);

    Long updateReadingStatus(Member member, Long mybookId, HistoryInfo historyInfo);

    /*
     * 내 서점 책 목록 조회
     * @param pageable
     * @param keyword
     * @param member
     * @return Page<MyBookStoreRes>
     */
    Page<MyBookStoreRes> getMyBookStore(Pageable pageable, String keyword, Member member);

    /*
     * 내 책 히스토리 조회
     * @param pageable
     * @param keyword
     * @param member
     * @return MyBookHistoryRes
     */
    MyBookHistoryRes getMyBookHistory(Pageable pageable, String keyword, Member member);

    /*
     * 내 책 통합 검색
     * @param pageable
     * @param query 검색어
     * @param member
     * @return MyBookSearchRes
     */
    MyBookSearchRes searchMyBooks(Pageable pageable, String query, Member member);
}
