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
import com.damda.domain.mybook.model.BookInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Collection collection = getOwnedCollection(memberId, collectionId);

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
     * 컬렉션 만들기
     */
    @Override
    @Transactional
    public CollectionRes createCollection(UUID memberId, CollectionReq req) {
        String name = normalizeName(req.getName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        Collection collection = collectionRepository.save(
                Collection.builder()
                        .member(member)
                        .name(name)
                        .build());

        return toCollectionRes(collection);
    }

    /**
     * 컬렉션 이름 수정
     */
    @Override
    @Transactional
    public CollectionRes updateCollection(UUID memberId, Long collectionId, UpdateCollectionReq req) {
        String name = normalizeName(req.getName());

        Collection collection = getOwnedCollection(memberId, collectionId);
        collection.updateName(name);

        return toCollectionRes(collection);
    }

    /**
     * 컬렉션에 담기 전 확인
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionBookCheckRes checkBook(UUID memberId, Long collectionId, Integer aladinId) {
        Collection collection = getOwnedCollection(memberId, collectionId);

        Optional<MyBook> shelfBook = findShelfBook(memberId, aladinId);

        boolean alreadyInCollection = shelfBook
                .map(myBook -> collectionBookRepository.existsByCollectionAndMybook(collection, myBook))
                .orElse(false);

        return CollectionBookCheckRes.builder()
                .onShelf(shelfBook.isPresent())
                .mybookId(shelfBook.map(MyBook::getMybookId).orElse(null))
                .alreadyInCollection(alreadyInCollection)
                .build();
    }

    /**
     * 컬렉션에 책 담기
     * - 서가에 이미 있는 책: 바로 컬렉션에 담는다.
     * - 서가에 없는 책: 서가에 추가할 때와 똑같이 서가에 저장(독서 상태/읽고 싶은 이유 포함)한 뒤 담는다.
     * - 같은 컬렉션에는 같은 책을 한 번만, 최대 9권, 새 책은 맨 앞의 빈 칸에 들어간다.
     */
    @Override
    @Transactional
    public CollectionBookRes addBook(UUID memberId, Long collectionId, CollectionBookReq req) {
        // 같은 컬렉션에 대한 담기/빼기를 직렬화 (칸 중복 방지)
        Collection collection = collectionRepository
                .findByCollectionIdAndMemberIdForUpdate(collectionId, memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION));

        // 서가에서 삭제된 책이 차지하고 있던 칸 정리
        collectionBookRepository.deleteInactiveBooks(collection);

        // 1. 서가에 이미 있는 책인지 확인
        Optional<MyBook> shelfBook = resolveShelfBook(memberId, req);

        // 2. 이미 이 컬렉션에 담긴 책이면 안내 (정책서: "이미 이 컬렉션에 담긴 책이에요")
        shelfBook.ifPresent(myBook -> {
            if (collectionBookRepository.existsByCollectionAndMybook(collection, myBook)) {
                throw new BaseException(ALREADY_IN_COLLECTION);
            }
        });

        // 3. 맨 앞의 빈 칸 찾기 (9권이 가득 찼으면 담을 수 없다)
        int slotNo = findFirstEmptySlot(collection);

        // 4. 서가에 없는 책이면 서가에 먼저 저장
        boolean savedToShelf = shelfBook.isEmpty();
        MyBook myBook = shelfBook.orElseGet(() -> saveToShelf(memberId, req));

        // 5. 컬렉션에 담기
        collectionBookRepository.save(CollectionBook.builder()
                .collection(collection)
                .mybook(myBook)
                .slotNo(slotNo)
                .build());

        // member의 lastBookActionAt 업데이트
        memberRepository.updateLastBookActionAt(memberId, LocalDateTime.now());

        return CollectionBookRes.builder()
                .collectionId(collection.getCollectionId())
                .mybookId(myBook.getMybookId())
                .slotNo(slotNo)
                .savedToShelf(savedToShelf)
                .build();
    }

    /**
     * 컬렉션에서 책 빼기
     * - 그 칸만 빈 칸이 되고 다른 책의 칸은 바뀌지 않는다.
     * - 서가의 책(나의 책)은 삭제되지 않는다.
     */
    @Override
    @Transactional
    public void removeBook(UUID memberId, Long collectionId, Long mybookId) {
        Collection collection = collectionRepository
                .findByCollectionIdAndMemberIdForUpdate(collectionId, memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION));

        CollectionBook collectionBook = collectionBookRepository
                .findByCollectionAndMybookId(collection, mybookId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION_BOOK));

        collectionBookRepository.delete(collectionBook);

        // member의 lastBookActionAt 업데이트
        memberRepository.updateLastBookActionAt(memberId, LocalDateTime.now());
    }

    // ------------------------------------------------------------------
    // private
    // ------------------------------------------------------------------

    /**
     * 내 컬렉션 조회 (없거나 내 컬렉션이 아니면 NOT_FOUND_COLLECTION)
     */
    private Collection getOwnedCollection(UUID memberId, Long collectionId) {
        return collectionRepository.findByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_COLLECTION));
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
     * 서가에 이미 있는 책 찾기 (같은 알라딘 ID)
     */
    private Optional<MyBook> findShelfBook(UUID memberId, Integer aladinId) {
        if (aladinId == null) {
            return Optional.empty();
        }
        return collectionBookRepository
                .findShelfBooksByMemberIdAndAladinId(memberId, String.valueOf(aladinId))
                .stream()
                .findFirst();
    }

    /**
     * 요청에서 서가의 책 결정
     * - mybookId 가 있으면 내 서가의 그 책
     * - bookInfo 가 있으면 알라딘 ID 로 서가에서 찾기 (없으면 empty = 새로 저장해야 함)
     */
    private Optional<MyBook> resolveShelfBook(UUID memberId, CollectionBookReq req) {
        if (req.getMybookId() != null) {
            MyBook myBook = myBookRepository.findByMybookIdAndStatusIs(req.getMybookId(), MyBook.Status.ACTIVE)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_MY_BOOK));
            if (!myBook.getMember().getMemberId().equals(memberId)) {
                throw new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
            }
            return Optional.of(myBook);
        }

        BookInfo bookInfo = req.getBookInfo();
        if (bookInfo == null) {
            throw new BaseException(INVALID_COLLECTION_BOOK_REQUEST);
        }
        return findShelfBook(memberId, bookInfo.getAladinId());
    }

    /**
     * 맨 앞의 빈 칸 번호 (1~9). 모두 차 있으면 COLLECTION_FULL
     */
    private int findFirstEmptySlot(Collection collection) {
        Set<Integer> used = new HashSet<>(collectionBookRepository.findSlotNosByCollection(collection));
        return IntStream.rangeClosed(1, Collection.MAX_BOOKS)
                .filter(slotNo -> !used.contains(slotNo))
                .findFirst()
                .orElseThrow(() -> new BaseException(COLLECTION_FULL));
    }

    /**
     * 서가에 책 저장 - 기존 MyBookService.addMyBook 을 그대로 사용한다.
     * (책/작가 생성, 독서 상태 결정, lastBookActionAt 갱신 등)
     */
    private MyBook saveToShelf(UUID memberId, CollectionBookReq req) {
        MyBookReq myBookReq = MyBookReq.builder()
                .bookInfo(req.getBookInfo())
                .historyInfo(req.getHistoryInfo())
                .reason(req.getReason())
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

    private CollectionRes toCollectionRes(Collection collection) {
        return CollectionRes.builder()
                .collectionId(collection.getCollectionId())
                .name(collection.getName())
                .createdAt(collection.getCreatedAt())
                .build();
    }
}
