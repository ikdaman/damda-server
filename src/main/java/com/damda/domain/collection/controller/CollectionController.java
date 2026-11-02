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
 *
 * 책을 담고 빼는 건별 API 는 두지 않는다.
 * 편집 화면에서 담기/빼기/이름 수정을 모두 끝낸 뒤 '완료'를 눌렀을 때,
 * 최종 상태를 한 번의 요청(생성 POST / 수정 PUT)으로 저장한다.
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
     * 컬렉션 만들기 (정책서 2-4, 2-5)
     * POST /collections
     * - 이름 + 최종 책 목록(선택)을 한 번에 저장한다. 응답은 만들어진 컬렉션의 상세(9칸).
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDetailRes> createCollection(
            @Valid @RequestBody CollectionReq dto,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CollectionDetailRes res = collectionService.createCollection(authMember.getMember().getMemberId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 컬렉션 수정 (정책서 2-5 이름 수정 / 책 담기 / 책 빼기)
     * PUT /collections/{collectionId}
     * - 편집 완료 시 이름 + 최종 책 목록으로 전체 교체한다. 응답은 수정된 컬렉션의 상세(9칸).
     * - 서가의 책(나의 책)은 삭제되지 않는다.
     */
    @PutMapping("/{collectionId}")
    public ResponseEntity<CollectionDetailRes> updateCollection(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long collectionId,
            @Valid @RequestBody UpdateCollectionReq dto
    ) {
        return ResponseEntity.ok(
                collectionService.updateCollection(authMember.getMember().getMemberId(), collectionId, dto));
    }
}
