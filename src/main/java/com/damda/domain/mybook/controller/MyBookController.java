package com.damda.domain.mybook.controller;

import com.damda.domain.mybook.model.*;
import com.damda.domain.mybook.service.MyBookService;
import com.damda.global.auth.model.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 나의 책 컨트롤러
 */
@RestController
@RequestMapping("/mybooks")
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    /**
     * 내 책 통합 검색
     * GET /mybooks?query=검색어
     * 정렬: 1순위 책 제목 정확도, 2순위 최신 등록순
     */
    @GetMapping
    public ResponseEntity<MyBookSearchRes> searchMyBooks(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @RequestParam String query,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ResponseEntity.ok(myBookService.searchMyBooks(pageable, query, authMember.getMember()));
    }

    @GetMapping("/store")
    public ResponseEntity<Page<MyBookStoreRes>> getMyBookStore(
            @PageableDefault(page = 0, size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ResponseEntity.ok(myBookService.getMyBookStore(pageable, keyword, authMember.getMember()));
    }

    @GetMapping("/history")
    public ResponseEntity<MyBookHistoryRes> getMyBookHistory(
            @PageableDefault(page = 0, size = 5, sort = "startedDate", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ResponseEntity.ok(myBookService.getMyBookHistory(pageable, keyword, authMember.getMember()));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyBookRes> addMyBook(
            @RequestBody MyBookReq dto,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        MyBookRes myBookRes = myBookService.addMyBook(authMember.getMember().getMemberId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{mybook_id}")
    public ResponseEntity<Void> deleteMyBook(@PathVariable Integer mybook_id,
                                             @AuthenticationPrincipal AuthMember authMember) {
        myBookService.deleteMyBook(authMember.getMember().getMemberId(), mybook_id);
        return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
    }

    @PatchMapping("/{mybookId}")
    public ResponseEntity updateMyBookStatus(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable(value = "mybookId") Long mybookId,
            @RequestBody UpdateMyBookReq myBookReq) {
        Map<String, Long> result = new HashMap<>();
        result.put("mybookId", myBookService.updateMyBook(authMember.getMember(), mybookId, myBookReq));

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{mybookId}/reading-status")
    public ResponseEntity updateMyBookReadingStatus(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable(value = "mybookId") Long mybookId,
            @RequestBody HistoryInfo historyInfo) {
        Map<String, Long> result = new HashMap<>();
        result.put("mybookId", myBookService.updateReadingStatus(authMember.getMember(), mybookId, historyInfo));

        return ResponseEntity.ok(result);
    }

    /**
     * 나의 책 상세 정보 조회
     * @param authMember Spring Security에서 인증된 회원 정보
     * @param mybookId 나의 책 ID
     * @return 나의 책 상세 정보
     */
    @GetMapping("/{mybookId}")
    public ResponseEntity<MyBookDetailRes> getMyBookDetail(@AuthenticationPrincipal AuthMember authMember,
                                                           @PathVariable Long mybookId) {
        MyBookDetailRes res = myBookService.getMyBookDetail(authMember.getMember().getMemberId(), mybookId);
        return ResponseEntity.ok(res);
    }
}