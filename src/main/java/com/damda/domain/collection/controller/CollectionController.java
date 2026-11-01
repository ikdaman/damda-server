package com.damda.domain.collection.controller;

import com.damda.domain.collection.model.*;
import com.damda.domain.collection.service.CollectionService;
import com.damda.global.auth.model.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 컬렉션 컨트롤러
 */
@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
@Slf4j
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 나의 컬렉션 목록 조회 (정책서 2-3)
     * GET /collections
     * 정렬: 만든 순서(오래된 것 → 최신), 주제 서가의 쪽 순서와 같다.
     */
    @GetMapping
    public ResponseEntity<CollectionListRes> getMyCollections(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ResponseEntity.ok(collectionService.getMyCollections(authMember.getMember().getMemberId()));
    }

    /**
     * 컬렉션 상세(주제 서가 한 쪽) 조회 (정책서 2-1)
     * GET /collections/{collectionId}
     */
    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailRes> getCollectionDetail(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId
    ) {
        return ResponseEntity.ok(
                collectionService.getCollectionDetail(authMember.getMember().getMemberId(), collectionId));
    }

    /**
     * 컬렉션 만들기 (정책서 2-4)
     * POST /collections
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionRes> createCollection(
            @Valid @RequestBody CollectionReq dto,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CollectionRes res = collectionService.createCollection(authMember.getMember().getMemberId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 컬렉션 이름 수정 (정책서 2-5)
     * PATCH /collections/{collectionId}
     */
    @PatchMapping("/{collectionId}")
    public ResponseEntity<CollectionRes> updateCollection(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionReq dto
    ) {
        return ResponseEntity.ok(
                collectionService.updateCollection(authMember.getMember().getMemberId(), collectionId, dto));
    }

    /**
     * 컬렉션에 담기 전 확인 (정책서 2-5)
     * GET /collections/{collectionId}/books/check?aladinId=123456
     * - onShelf=true  → 저장 시트 없이 mybookId 로 바로 담기
     * - onShelf=false → 저장 시트(상태/읽고 싶은 이유/날짜)를 거쳐 bookInfo 로 담기
     * - alreadyInCollection=true → "이미 이 컬렉션에 담긴 책이에요" 안내
     */
    @GetMapping("/{collectionId}/books/check")
    public ResponseEntity<CollectionBookCheckRes> checkBook(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId,
            @RequestParam Integer aladinId
    ) {
        return ResponseEntity.ok(
                collectionService.checkBook(authMember.getMember().getMemberId(), collectionId, aladinId));
    }

    /**
     * 컬렉션에 책 담기 (정책서 2-5)
     * POST /collections/{collectionId}/books
     */
    @PostMapping(value = "/{collectionId}/books", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionBookRes> addBook(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId,
            @Valid @RequestBody CollectionBookReq dto
    ) {
        CollectionBookRes res = collectionService.addBook(authMember.getMember().getMemberId(), collectionId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 컬렉션에서 책 빼기 (정책서 2-5)
     * DELETE /collections/{collectionId}/books/{mybookId}
     * - 서가의 책은 삭제되지 않고, 그 칸만 빈 칸이 된다.
     */
    @DeleteMapping("/{collectionId}/books/{mybookId}")
    public ResponseEntity<Void> removeBook(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId,
            @PathVariable Long mybookId
    ) {
        collectionService.removeBook(authMember.getMember().getMemberId(), collectionId, mybookId);
        return ResponseEntity.noContent().build();
    }
}
