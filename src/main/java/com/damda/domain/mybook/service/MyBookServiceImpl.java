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
import com.damda.domain.mybook.model.BookInfo;
import com.damda.domain.mybook.model.HistoryInfo;
import com.damda.domain.mybook.model.MyBookReq;
import com.damda.domain.mybook.model.MyBookRes;
import com.damda.domain.mybook.repository.MyBookRepository;
import com.damda.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
}
