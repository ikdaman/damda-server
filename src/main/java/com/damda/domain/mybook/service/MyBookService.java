package com.damda.domain.mybook.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.mybook.model.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
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
     */
    PageRes<MyBookStorePageRes.BookItem> getMyBookStore(Pageable pageable, String keyword, Member member);

    /*
     * 내 책 히스토리 조회
     */
    PageRes<MyBookHistoryRes.BookItem> getMyBookHistory(Pageable pageable, String keyword, Member member);

    /*
     * 내 책 통합 검색
     */
    PageRes<MyBookSearchRes.BookItem> searchMyBooks(Pageable pageable, String query, Member member);

    /*
     * 기록 추가
     */
    Map<String, Long> addRecord(UUID memberId, Long mybookId, RecordReq recordReq);

    /*
     * 기록 조회
     */
    RecordRes getRecord(UUID memberId, Long mybookId, Long recordId);

    /*
     * 기록 삭제
     */
    void deleteRecord(UUID memberId, Long mybookId, Long recordId);

    /*
     * 기록 전체 조회
     */
    List<RecordRes> getAllRecords(UUID memberId, Long mybookId);

    /*
     * 책 확인
     */
    Map<String, Long> checkMyBook(UUID memberId, Long aladinId);
}
