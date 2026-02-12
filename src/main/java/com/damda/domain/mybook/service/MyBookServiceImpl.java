package com.damda.domain.mybook.service;

import com.damda.domain.book.entity.Author;
import com.damda.domain.book.entity.Book;
import com.damda.domain.book.entity.Writer;
import com.damda.domain.book.repository.AuthorRepository;
import com.damda.domain.book.repository.BookRepository;
import com.damda.domain.book.repository.WriterRepository;
import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.mybook.model.*;
import com.damda.domain.mybook.repository.MyBookRepository;
import com.damda.global.exception.BaseException;
import com.damda.global.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.damda.global.exception.ErrorCode.*;

/**
 * 나의 책 서비스 구현체
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MyBookServiceImpl implements MyBookService {

    private final MyBookRepository myBookRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final WriterRepository writerRepository;
    private final MemberRepository memberRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;


    @Override
    @Transactional(readOnly = true)
    public Page<MyBookStoreRes> getMyBookStore(Pageable pageable, String keyword, Member member) {
        Page<MyBook> myBooks = myBookRepository.findAllByMemberAndReadingStatusAndKeyword(
                member,
                MyBook.ReadingStatus.TODO,
                keyword,
                pageable
        );

        return myBooks.map(myBook -> MyBookStoreRes.builder()
                .mybookId(myBook.getMybookId())
                .createdDate(myBook.getCreatedAt())
                .bookInfo(MyBookStoreRes.BookInfo.builder()
                        .title(myBook.getBook().getTitle())
                        .author(myBook.getBook().getAuthor().stream()
                                .map(author -> author.getWriter().getWriterName())
                                .collect(Collectors.toList()))
                        .coverImage(myBook.getBook().getCoverImage())
                        .description(myBook.getBook().getDescription())
                        .build())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public MyBookHistoryRes getMyBookHistory(Pageable pageable, String keyword, Member member) {
        Page<MyBook> myBooks = myBookRepository.findAllByMemberAndReadingStatusesAndKeyword(
                member,
                List.of(MyBook.ReadingStatus.INPROGRESS, MyBook.ReadingStatus.DONE),
                keyword,
                pageable
        );

        List<MyBookHistoryRes.BookItem> bookItems = myBooks.getContent().stream()
                .map(myBook -> MyBookHistoryRes.BookItem.builder()
                        .mybookId(myBook.getMybookId())
                        .startedDate(myBook.getStartedDate())
                        .finishedDate(myBook.getFinishedDate())
                        .bookInfo(MyBookHistoryRes.BookInfo.builder()
                                .title(myBook.getBook().getTitle())
                                .author(myBook.getBook().getAuthor().stream()
                                        .map(author -> author.getWriter().getWriterName())
                                        .collect(Collectors.toList()))
                                .coverImage(myBook.getBook().getCoverImage())
                                .description(myBook.getBook().getDescription())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return MyBookHistoryRes.builder()
                .totalPages(myBooks.getTotalPages())
                .nowPage(myBooks.getNumber())
                .books(bookItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MyBookSearchRes searchMyBooks(Pageable pageable, String query, Member member) {
        Page<MyBook> myBooks = myBookRepository.searchByQuery(member, query, pageable);

        List<MyBookSearchRes.BookItem> bookItems = myBooks.getContent().stream()
                .map(myBook -> MyBookSearchRes.BookItem.builder()
                        .mybookId(myBook.getMybookId())
                        .readingStatus(myBook.getReadingStatus())
                        .createdDate(myBook.getCreatedAt())
                        .startedDate(myBook.getStartedDate())
                        .finishedDate(myBook.getFinishedDate())
                        .bookInfo(MyBookSearchRes.BookInfo.builder()
                                .title(myBook.getBook().getTitle())
                                .author(myBook.getBook().getAuthor().stream()
                                        .map(author -> author.getWriter().getWriterName())
                                        .collect(Collectors.toList()))
                                .coverImage(myBook.getBook().getCoverImage())
                                .description(myBook.getBook().getDescription())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return MyBookSearchRes.builder()
                .totalPages(myBooks.getTotalPages())
                .nowPage(myBooks.getNumber())
                .totalElements(myBooks.getTotalElements())
                .books(bookItems)
                .build();
    }


    /**
     * 나의 책 추가
     */
    @Override
    @Transactional
    public MyBookRes addMyBook(UUID memberId, MyBookReq dto) {
        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        BookInfo bookInfo = dto.getBookInfo();

        // Writer 조회 또는 생성
        Writer writer = writerRepository.findByWriterName(bookInfo.getAuthor())
                .orElseGet(() -> writerRepository.save(
                        Writer.builder()
                                .writerName(bookInfo.getAuthor())
                                .build()
                ));

        // Book 조회 또는 생성
        Book book;
        if (bookInfo.getAladinId() != null && String.valueOf(bookInfo.getAladinId()).isEmpty()) {
            // Aladin Id가 있는 경우 (ALADIN: 직접 추가)
            book = bookRepository.findByAladinId(String.valueOf(bookInfo.getAladinId()))
                    .orElseGet(() -> createNewMyBook(bookInfo));
        } else {
            // Aladin Id가 없는 경우 (CUSTOM: 직접 추가)
            book = createNewMyBook(bookInfo);
        }

        // Author 관계 생성
        if (!authorRepository.existsByBookAndWriter(book, writer)) {
            authorRepository.save(Author.builder()
                    .book(book)
                    .writer(writer)
                    .build());
        }

        // 한 사용자가 책장에 같은 책을 중복으로 저장할 수 없음 (ACTIVE 상태만 체크)
        if(myBookRepository.existsByMemberAndBookAndStatus(member, book, MyBook.Status.ACTIVE)) {
            throw new BaseException(MY_BOOK_ALREADY_EXISTS);
        }

        // ReadingStatus 결정
        MyBook.ReadingStatus readingStatus = determineReadingStatus(dto.getHistoryInfo());

        // MyBook 생성
        MyBook.MyBookBuilder myBookBuilder = MyBook.builder()
                .member(member)
                .book(book)
                .reason(dto.getReason())
                .readingStatus(readingStatus)
                .status(MyBook.Status.ACTIVE);

        // HistoryInfo가 있는 경우 날짜 설정
        if (dto.getHistoryInfo() != null) {
            if (dto.getHistoryInfo().getStartedDate() != null) {
                myBookBuilder.startedDate(dto.getHistoryInfo().getStartedDate());
            }
            if (dto.getHistoryInfo().getFinishedDate() != null) {
                myBookBuilder.finishedDate(dto.getHistoryInfo().getFinishedDate());
            }
        }

        MyBook myBook = myBookBuilder.build();
        myBookRepository.save(myBook);

        return MyBookRes.builder()
                .mybookId(Math.toIntExact(myBook.getMybookId()))
                .title(bookInfo.getTitle())
                .writer(bookInfo.getAuthor())
                .itemId(bookInfo.getAladinId())
                .reason(dto.getReason())
                .createdAt(String.valueOf(myBook.getCreatedAt())) // BaseTime에서 자동 생성된 값
                .build();
    }

    /**
     * 새로운 MyBook 생성
     */
    private Book createNewMyBook(BookInfo bookInfo) {
        Book newBook = Book.builder()
                .title(bookInfo.getTitle())
                .publisher(bookInfo.getPublisher())
                .isbn(bookInfo.getIsbn())
                .totalPage(bookInfo.getTotalPage())
                .coverImage(bookInfo.getCoverImage())
                .aladinId(bookInfo.getAladinId() != null ?
                        String.valueOf(bookInfo.getAladinId()) : null)
                .source(Book.Source.valueOf(bookInfo.getSource()))
                .description(bookInfo.getDescription())
                .publishDate(bookInfo.getPublishDate())
                .build();

        return bookRepository.save(newBook);
    }

    /**
     * HistoryInfo를 기반으로 ReadingStatus 결정
     */
    private MyBook.ReadingStatus determineReadingStatus(HistoryInfo historyInfo) {
        if (historyInfo == null) {
            return MyBook.ReadingStatus.TODO;
        }

        // 완독일이 있으면 DONE
        if (historyInfo.getFinishedDate() != null) {
            return MyBook.ReadingStatus.DONE;
        }

        // 시작일이 있으면 INPROGRESS
        if (historyInfo.getStartedDate() != null) {
            return MyBook.ReadingStatus.INPROGRESS;
        }

        // 둘 다 없으면 TODO
        return MyBook.ReadingStatus.TODO;
    }

    /**
     * 나의 책 삭제
     */
    @Override
    @Transactional
    public void deleteMyBook(UUID memberId, Integer id) {
        MyBook myBook = myBookRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new BaseException(NOT_FOUND_MY_BOOK));

        if (!myBook.getMember().getMemberId().equals(memberId)) {
            throw new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
        }

        // SOFT DELETE: status를 INACTIVE로 변경
        myBook.updateToInactive();
    }

    @Override
    @Transactional
    public Long updateMyBook(Member member, Long mybookId, UpdateMyBookReq myBookReq) {
        MyBook mybook = myBookRepository.findByMybookIdAndStatusIs(mybookId, MyBook.Status.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FOUND_MY_BOOK));

        if (!member.getMemberId().equals(mybook.getMember().getMemberId())) {
            throw new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
        }

        // 이유 업데이트
        myBookReq.getReason().ifPresent(reason -> {
            if(reason.isBlank()) {
                throw new BaseException(MYBOOK_REASON_CANNOT_BE_EMPTY);
            }
            mybook.updateReason(reason);
        });

        // historyInfo 업데이트
        myBookReq.getHistoryInfo().ifPresent(historyInfo -> {
            // startedDate 업데이트
            historyInfo.getStartedDate().ifPresent(stringDate -> {
                LocalDateTime startedDate = StringUtils.toLocalDateTime(stringDate);
                mybook.updateStartedDate(startedDate);
            });
            // finishedDate 업데이트
            historyInfo.getFinishedDate().ifPresent(stringDate -> {
                LocalDateTime finishedDate = StringUtils.toLocalDateTime(stringDate);
                mybook.updateFinishedDate(finishedDate);
            });
            // readingStatus 업데이트
            mybook.updateReadingStatus();
        });

        // bookInfo 업데이트
        if(mybook.getBook().getSource() == Book.Source.CUSTOM) {
            myBookReq.getBookInfo().ifPresent(bookInfo -> {
                // title 업데이트
                bookInfo.getTitle().ifPresent(title -> {
                    if(title.isBlank()) {
                        throw new BaseException(BOOK_TITLE_CANNOT_BE_EMPTY);
                    }
                    mybook.getBook().updateTitle(title);
                });

                // author 업데이트
                bookInfo.getAuthor().ifPresent(author -> {
                    // Writer 조회 또는 생성
                    Writer writer = writerRepository.findByWriterName(author)
                            .orElseGet(() -> writerRepository.save(
                                    Writer.builder()
                                            .writerName(author)
                                            .build()
                            ));

                    // Author 관계 생성
                    if (!authorRepository.existsByBookAndWriter(mybook.getBook(), writer)) {
                        authorRepository.save(Author.builder()
                                .book(mybook.getBook())
                                .writer(writer)
                                .build());
                    }
                });

                // publisher 업데이트
                bookInfo.getPublisher().ifPresent(publisher -> mybook.getBook().updatePublisher(publisher));
                // publishDate 업데이트
                bookInfo.getPublishDate().ifPresent(stringDate -> {
                    LocalDate publishDate = StringUtils.toLocalDate(stringDate);
                    LocalDateTime publishedDateTime = null;

                    if(publishDate != null) {
                        publishedDateTime = publishDate.atStartOfDay();
                    }

                    mybook.getBook().updatePublishDate(publishedDateTime);
                });
                // isbn 업데이트
                bookInfo.getIsbn().ifPresent(isbn -> mybook.getBook().updateIsbn(isbn));
                // totalPage 업데이트
                bookInfo.getTotalPage().ifPresent(totalPage -> {
                    Integer page = null;
                    if(totalPage > 0) {
                        page = totalPage;
                    }
                    mybook.getBook().updateTotalPage(page);
                });
            });
        }

        myBookRepository.save(mybook);

        return mybook.getMybookId();
    }

    @Override
    @Transactional
    public Long updateReadingStatus(Member member, Long mybookId, HistoryInfo historyInfo) {
        MyBook mybook = myBookRepository.findByMybookIdAndStatusIs(mybookId, MyBook.Status.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FOUND_MY_BOOK));

        if (!member.getMemberId().equals(mybook.getMember().getMemberId())) {
            throw new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
        }

        // startedDate 업데이트
        if(historyInfo.getStartedDate() != null) {
            mybook.updateStartedDate(historyInfo.getStartedDate());
        }

        // finishedDate 업데이트
        mybook.updateFinishedDate(historyInfo.getFinishedDate());

        // readingStatus 업데이트
        mybook.updateReadingStatus();
        myBookRepository.save(mybook);

        return mybook.getMybookId();
    }

    /**
     * 나의 책 상세 조회
     * @param memberId 회원 ID
     * @param mybookId 나의 책 ID
     * @return 나의 책 상세 정보
     */
    @Override
    @Transactional(readOnly = true)
    public MyBookDetailRes getMyBookDetail(UUID memberId, Long mybookId) {

        // 1. 나의 책 조회 (회원 소유 확인 포함)
        MyBook myBook = myBookRepository.findByMybookIdAndMemberId(mybookId, memberId)
                .orElseThrow(() -> {
//                    log.warn("나의 책 조회 실패: mybookId={}, memberId={}", mybookId, memberId);
                    return new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
                });
        Book book = myBook.getBook();

        // 2. 작가명 리스트 생성 (콤마로 구분)
        List<Author> authors = book.getAuthor();
        String authorNames = authors.stream()
                .map(author -> author.getWriter().getWriterName())
                .collect(Collectors.joining(", "));

        // 3. BookInfo 생성
        MyBookDetailRes.BookInfo bookInfo = MyBookDetailRes.BookInfo.builder()
                .bookId(String.valueOf(book.getBookId()))
                .source(book.getSource().name())  // ALADIN or CUSTOM
                .title(book.getTitle())
                .author(authorNames)
                .coverImage(book.getCoverImage())
                .publisher(book.getPublisher())
                .totalPage(book.getTotalPage())
                .publishDate(book.getPublishDate() != null
                        ? book.getPublishDate().format(DATE_FORMATTER)
                        : null)
                .isbn(book.getIsbn())
                .aladinId(book.getAladinId())
                .build();

        // 4. HistoryInfo 생성
        MyBookDetailRes.HistoryInfo historyInfo = MyBookDetailRes.HistoryInfo.builder()
                .startedDate(myBook.getStartedDate() != null
                        ? myBook.getStartedDate().format(DATE_FORMATTER)
                        : null)
                .finishedDate(myBook.getFinishedDate() != null
                        ? myBook.getFinishedDate().format(DATE_FORMATTER)
                        : null)
                .build();

        // 5. MyBookDetailRes 생성
        MyBookDetailRes response = MyBookDetailRes.builder()
                .bookInfo(bookInfo)
                .historyInfo(historyInfo)
                .mybookId(String.valueOf(myBook.getMybookId()))
                .readingStatus(myBook.getReadingStatus().name())  // TODO, INPROGRESS, DONE
                .shelfType(myBook.getShelfType())  // STORE or HISTORY
                .createdDate(myBook.getCreatedAt().format(DATE_FORMATTER))
                .reason(myBook.getReason())
                .build();

//        log.info("나의 책 조회 완료: mybookId={}, memberId={}, shelfType={}",
//                mybookId, memberId, response.getShelfType());
        return response;
    }

    /**
     * 책 주인 확인
     * @param mybookId
     * @param memberId
     * @return
     */
    private MyBook getMyBookIfOwner(Long mybookId, UUID memberId) {
        MyBook myBook = myBookRepository.findById(mybookId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_BOOK));
        if (!myBook.getMember().getMemberId().equals(memberId)) {
            throw new BaseException(BOOK_NOT_OWNED_BY_MEMBER);
        }
        return myBook;
    }
}
