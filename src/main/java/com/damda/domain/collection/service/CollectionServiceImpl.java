package com.damda.domain.collection.service;

import com.damda.domain.book.entity.Book;
import com.damda.domain.collection.entity.Collection;
import com.damda.domain.collection.entity.CollectionBook;
import com.damda.domain.collection.model.*;
import com.damda.domain.collection.repository.CollectionBookRepository;
import com.damda.domain.collection.repository.CollectionRepository;
import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.mybook.model.MyBookReq;
import com.damda.domain.mybook.model.MyBookRes;
import com.damda.domain.mybook.repository.MyBookRepository;
import com.damda.domain.mybook.service.MyBookService;
import com.damda.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.damda.global.exception.ErrorCode.*;

/**
 * 컬렉션 서비스 구현체
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    /** 나의 컬렉션 목록의 대표 표지 수 */
    private static final int REPRESENTATIVE_COVER_COUNT = 2;

    private final CollectionRepository collectionRepository;
    private final CollectionBookRepository collectionBookRepository;
    private final MyBookRepository myBookRepository;
    private final MemberRepository memberRepository;
    private final MyBookService myBookService;

    /**
     * 나의 컬렉션 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionListRes getMyCollections(UUID memberId) {
        List<Collection> collections = collectionRepository.findAllByMemberId(memberId);

        // 컬렉션별 담긴 책 (칸 번호 순) - 한 번에 조회 후 그룹핑
        Map<Long, List<CollectionBook>> booksByCollection = collectionBookRepository
                .findAllWithBookByMemberId(memberId).stream()
                .collect(Collectors.groupingBy(cb -> cb.getCollection().getCollectionId()));

        List<CollectionListRes.CollectionItem> items = collections.stream()
                .map(collection -> CollectionListRes.CollectionItem.builder()
                        .collectionId(collection.getCollectionId())
                        .name(collection.getName())
                        .representativeBooks(
                                booksByCollection.getOrDefault(collection.getCollectionId(), List.of()).stream()
                                        .limit(REPRESENTATIVE_COVER_COUNT)
                                        .map(cb -> CollectionListRes.RepresentativeBook.builder()
                                                .mybookId(cb.getMybook().getMybookId())
                                                .coverImage(cb.getMybook().getBook().getCoverImage())
                                                .build())
                                        .toList())
                        .createdAt(collection.getCreatedAt())
                        .build())
                .toList();

        return CollectionListRes.builder()
                .totalCount(items.size())
                .collections(items)
                .build();
    }

    /**
     * 컬렉션 상세(주제 서가 한 쪽) 조회 - 항상 9칸으로 내려준다.
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionDetailRes getCollectionDetail(UUID memberId, Long collectionId) {
        Collection collection = collectionRepository.findByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION));

        return buildDetail(collection);
    }

    /**
     * 컬렉션 만들기: 이름 + 최종 책 목록(선택)을 한 번에 저장
     */
    @Override
    @Transactional
    public CollectionDetailRes createCollection(UUID memberId, CollectionReq req) {
        String name = normalizeName(req.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        Collection collection = collectionRepository.save(
                Collection.builder()
                        .member(member)
                        .name(name)
                        .build());

        if (req.getBooks() != null && !req.getBooks().isEmpty()) {
            applyBooks(memberId, collection, req.getBooks());
        }

        return buildDetail(collection);
    }

    /**
     * 컬렉션 수정: 이름 + 최종 책 목록으로 전체 교체
     * - 같은 컬렉션에 대한 수정이 동시에 들어와도 칸이 겹치지 않도록 컬렉션 행을 잠근다.
     */
    @Override
    @Transactional
    public CollectionDetailRes updateCollection(UUID memberId, Long collectionId, UpdateCollectionReq req) {
        String name = normalizeName(req.getName());

        Collection collection = collectionRepository
                .findByCollectionIdAndMemberIdForUpdate(collectionId, memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION));

        collection.updateName(name);
        applyBooks(memberId, collection, req.getBooks());

        return buildDetail(collection);
    }

    // ------------------------------------------------------------------
    // private
    // ------------------------------------------------------------------

    /**
     * 최종 책 목록을 컬렉션에 반영한다. (정책서 2-5)
     *
     * 1. 모든 검증을 먼저 끝낸다. (하나라도 틀리면 아무것도 저장하지 않는다)
     *    - 최대 9권, 같은 책 중복 없음, 칸 번호 범위/중복, 내 서가의 책인지
     * 2. 서가에 없는 책은 서가에 저장한다. (기존 MyBookService.addMyBook 사용)
     * 3. 칸 배정: slotNo 를 보낸 책은 그 칸, 생략한 책은 맨 앞의 빈 칸부터 요청 순서대로
     * 4. 기존 연결과 비교해서 바뀐 것만 삭제/추가한다. (그대로인 책은 건드리지 않는다)
     */
    private void applyBooks(UUID memberId, Collection collection, List<CollectionBookReq> items) {
        // 서가에서 삭제된 책이 차지하고 있던 칸 정리
        collectionBookRepository.deleteInactiveBooks(collection);

        if (items.size() > Collection.MAX_BOOKS) {
            throw new BaseException(COLLECTION_FULL);
        }

        // ---- 서가의 책 일괄 조회 (쿼리 2회) ----
        List<Long> mybookIds = items.stream()
                .map(CollectionBookReq::getMybookId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, MyBook> shelfById = mybookIds.isEmpty()
                ? Map.of()
                : collectionBookRepository.findShelfBooksByIds(memberId, mybookIds).stream()
                        .collect(Collectors.toMap(MyBook::getMybookId, mb -> mb, (a, b) -> a));

        List<String> aladinIds = items.stream()
                .filter(item -> item.getMybookId() == null
                        && item.getBookInfo() != null
                        && item.getBookInfo().getAladinId() != null)
                .map(item -> String.valueOf(item.getBookInfo().getAladinId()))
                .distinct()
                .toList();
        Map<String, MyBook> shelfByAladinId = aladinIds.isEmpty()
                ? Map.of()
                : collectionBookRepository.findShelfBooksByMemberIdAndAladinIds(memberId, aladinIds).stream()
                        .collect(Collectors.toMap(mb -> mb.getBook().getAladinId(), mb -> mb, (a, b) -> a));

        // ---- 검증 + 항목별 서가의 책 결정 (null = 서가에 없어 새로 저장해야 함) ----
        List<MyBook> resolved = new ArrayList<>();
        Set<Long> usedMybookIds = new HashSet<>();
        Set<String> newAladinIds = new HashSet<>();
        Set<Integer> explicitSlots = new HashSet<>();

        for (CollectionBookReq item : items) {
            MyBook shelfBook;
            if (item.getMybookId() != null) {
                shelfBook = shelfById.get(item.getMybookId());
                if (shelfBook == null) {
                    throw new BaseException(NOT_FOUND_MY_BOOK);
                }
            } else if (item.getBookInfo() != null) {
                Integer aladinId = item.getBookInfo().getAladinId();
                shelfBook = aladinId == null ? null : shelfByAladinId.get(String.valueOf(aladinId));
            } else {
                throw new BaseException(INVALID_COLLECTION_BOOK_REQUEST);
            }

            // 같은 컬렉션에는 같은 책을 한 번만 (정책서: "이미 이 컬렉션에 담긴 책이에요")
            if (shelfBook != null) {
                if (!usedMybookIds.add(shelfBook.getMybookId())) {
                    throw new BaseException(ALREADY_IN_COLLECTION);
                }
            } else if (item.getBookInfo().getAladinId() != null
                    && !newAladinIds.add(String.valueOf(item.getBookInfo().getAladinId()))) {
                throw new BaseException(ALREADY_IN_COLLECTION);
            }

            Integer slotNo = item.getSlotNo();
            if (slotNo != null
                    && (slotNo < 1 || slotNo > Collection.MAX_BOOKS || !explicitSlots.add(slotNo))) {
                throw new BaseException(INVALID_COLLECTION_BOOK_REQUEST);
            }

            resolved.add(shelfBook);
        }

        // ---- 칸 배정: slotNo 가 없는 책은 맨 앞의 빈 칸부터 ----
        Iterator<Integer> freeSlots = IntStream.rangeClosed(1, Collection.MAX_BOOKS)
                .filter(n -> !explicitSlots.contains(n))
                .iterator();

        // ---- 서가에 없는 책 저장 + 최종 (나의 책 ID -> 칸) ----
        Map<Long, Integer> finalSlots = new LinkedHashMap<>();
        Map<Long, MyBook> finalBooks = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            CollectionBookReq item = items.get(i);
            MyBook myBook = resolved.get(i) != null ? resolved.get(i) : saveToShelf(memberId, item);
            int slotNo = item.getSlotNo() != null ? item.getSlotNo() : freeSlots.next();
            finalSlots.put(myBook.getMybookId(), slotNo);
            finalBooks.put(myBook.getMybookId(), myBook);
        }

        // ---- 기존 연결과 비교: 빠진 책/칸이 바뀐 책은 삭제, 새 책/칸이 바뀐 책은 추가 ----
        Set<Long> kept = new HashSet<>();
        List<CollectionBook> toDelete = new ArrayList<>();
        for (CollectionBook cb : collectionBookRepository.findAllByCollection(collection)) {
            Long mybookId = cb.getMybook().getMybookId();
            Integer slotNo = finalSlots.get(mybookId);
            if (slotNo != null && slotNo.equals(cb.getSlotNo())) {
                kept.add(mybookId);
            } else {
                toDelete.add(cb);
            }
        }

        // 삭제를 먼저 반영해야 칸이 바뀐 책을 같은 칸 제약(uk_collection_book_slot) 충돌 없이 다시 넣을 수 있다.
        collectionBookRepository.deleteAll(toDelete);
        collectionBookRepository.flush();

        int inserted = 0;
        for (Map.Entry<Long, Integer> entry : finalSlots.entrySet()) {
            if (kept.contains(entry.getKey())) {
                continue;
            }
            collectionBookRepository.save(CollectionBook.builder()
                    .collection(collection)
                    .mybook(finalBooks.get(entry.getKey()))
                    .slotNo(entry.getValue())
                    .build());
            inserted++;
        }

        if (inserted > 0 || !toDelete.isEmpty()) {
            // member의 lastBookActionAt 업데이트
            memberRepository.updateLastBookActionAt(memberId, LocalDateTime.now());
        }
    }

    /**
     * 컬렉션 상세 응답 생성 - 항상 9칸
     */
    private CollectionDetailRes buildDetail(Collection collection) {
        Map<Integer, CollectionBook> bySlot = collectionBookRepository
                .findAllWithBookByCollection(collection).stream()
                .collect(Collectors.toMap(CollectionBook::getSlotNo, cb -> cb));

        List<CollectionDetailRes.Slot> slots = IntStream.rangeClosed(1, Collection.MAX_BOOKS)
                .mapToObj(slotNo -> {
                    CollectionBook cb = bySlot.get(slotNo);
                    if (cb == null) {
                        return CollectionDetailRes.Slot.empty(slotNo);
                    }
                    MyBook myBook = cb.getMybook();
                    return CollectionDetailRes.Slot.builder()
                            .slotNo(slotNo)
                            .empty(false)
                            .mybookId(myBook.getMybookId())
                            .reason(myBook.getReason())
                            .bookInfo(toBookInfo(myBook.getBook()))
                            .build();
                })
                .toList();

        return CollectionDetailRes.builder()
                .collectionId(collection.getCollectionId())
                .name(collection.getName())
                .createdAt(collection.getCreatedAt())
                .slots(slots)
                .build();
    }

    /**
     * 컬렉션 이름 정리 (앞뒤 공백 제거, 빈 이름 거부)
     */
    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BaseException(COLLECTION_NAME_CANNOT_BE_EMPTY);
        }
        return name.trim();
    }

    /**
     * 서가에 책 저장 - 기존 MyBookService.addMyBook 을 그대로 사용한다.
     * (책/작가 생성, 독서 상태 결정, lastBookActionAt 갱신 등)
     */
    private MyBook saveToShelf(UUID memberId, CollectionBookReq item) {
        MyBookReq myBookReq = MyBookReq.builder()
                .bookInfo(item.getBookInfo())
                .historyInfo(item.getHistoryInfo())
                .reason(item.getReason())
                .build();

        MyBookRes saved = myBookService.addMyBook(memberId, myBookReq);

        return myBookRepository.findByMybookIdAndStatusIs(Long.valueOf(saved.getMybookId()), MyBook.Status.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FOUND_MY_BOOK));
    }

    private CollectionDetailRes.BookInfo toBookInfo(Book book) {
        return CollectionDetailRes.BookInfo.builder()
                .title(book.getTitle())
                .author(book.getAuthor() != null
                        ? book.getAuthor().stream()
                                .map(author -> author.getWriter().getWriterName())
                                .toList()
                        : List.of())
                .coverImage(book.getCoverImage())
                .build();
    }
}
