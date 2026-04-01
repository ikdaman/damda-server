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
import com.damda.domain.mybook.model.MyBookStorePageRes;
import com.damda.domain.mybook.model.PageRes;
import com.damda.domain.mybook.repository.MyBookRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class MyBookServiceImplTest {

    @Autowired
    private MyBookService myBookService;

    @Autowired
    private MyBookRepository myBookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private WriterRepository writerRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager entityManager;

    private Member testMember;

    @Test
    @Transactional
    @DisplayName("커스텀 책 정보가 최소한만 입력된 경우 내 서점 조회 시 500 에러가 발생하지 않아야 한다")
    void getMyBookStore_WithMinimalCustomBookInfo_ShouldNotThrow500Error() {
        // Given: 테스트 회원 생성
        testMember = Member.builder()
                .nickname("테스터1")
                .provider(Member.Provider.KAKAO)
                .providerId("test-provider-id-1")
                .status(Member.Status.ACTIVE)
                .build();
        memberRepository.save(testMember);
        
        // 커스텀 책을 최소 정보만으로 생성 (작가 정보 없음)
        Book customBook = Book.builder()
                .title("최소 정보 커스텀 책")
                .source(Book.Source.CUSTOM)
                .isbn("9780000000000")
                .publisher("Unknown")
                .totalPage(0)
                .build();
        bookRepository.save(customBook);

        // MyBook 생성 (CUSTOM 책이므로 Author 정보가 없을 수 있음)
        MyBook myBook = MyBook.builder()
                .member(testMember)
                .book(customBook)
                .reason("나중에 읽을 책")
                .readingStatus(MyBook.ReadingStatus.TODO)
                .status(MyBook.Status.ACTIVE)
                .build();
        myBookRepository.save(myBook);

        // When: 내 서점 조회
        Pageable pageable = PageRequest.of(0, 5);
        PageRes<MyBookStorePageRes.BookItem> result = myBookService.getMyBookStore(pageable, null, testMember);

        // Then: 에러 없이 조회되어야 하며, author 리스트는 빈 배열이어야 함
        assertThat(result).isNotNull();
        assertThat(result.getBooks()).hasSize(1);
        
        MyBookStorePageRes.BookItem bookItem = result.getBooks().get(0);
        assertThat(bookItem.getBookInfo().getTitle()).isEqualTo("최소 정보 커스텀 책");
        assertThat(bookItem.getBookInfo().getAuthor()).isNotNull();
        assertThat(bookItem.getBookInfo().getAuthor()).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("커스텀 책에 작가 정보를 추가한 경우 내 서점 조회 시 작가명이 표시되어야 한다")
    void getMyBookStore_WithCustomBookAndAuthor_ShouldReturnAuthorName() {
        // Given: 테스트 회원 생성
        testMember = Member.builder()
                .nickname("테스터2")
                .provider(Member.Provider.KAKAO)
                .providerId("test-provider-id-2")
                .status(Member.Status.ACTIVE)
                .build();
        memberRepository.save(testMember);
        
        // 커스텀 책 + 작가 정보
        Book customBook = Book.builder()
                .title("작가 있는 커스텀 책")
                .source(Book.Source.CUSTOM)
                .isbn("9780000000001")
                .publisher("테스트 출판사")
                .totalPage(0)
                .build();
        bookRepository.save(customBook);

        Writer writer = Writer.builder()
                .writerName("홍길동")
                .build();
        writerRepository.save(writer);

        Author author = Author.builder()
                .book(customBook)
                .writer(writer)
                .build();
        authorRepository.save(author);

        MyBook myBook = MyBook.builder()
                .member(testMember)
                .book(customBook)
                .reason("좋은 책")
                .readingStatus(MyBook.ReadingStatus.TODO)
                .status(MyBook.Status.ACTIVE)
                .build();
        myBookRepository.save(myBook);

        // 영속성 컨텍스트 강제 플러시 및 클리어
        entityManager.flush();
        entityManager.clear();

        // When
        Pageable pageable = PageRequest.of(0, 5);
        PageRes<MyBookStorePageRes.BookItem> result = myBookService.getMyBookStore(pageable, null, testMember);

        // Then
        assertThat(result.getBooks()).hasSize(1);
        MyBookStorePageRes.BookItem bookItem = result.getBooks().get(0);
        assertThat(bookItem.getBookInfo().getAuthor()).hasSize(1);
        assertThat(bookItem.getBookInfo().getAuthor().get(0)).isEqualTo("홍길동");
    }

    @Test
    @Transactional
    @DisplayName("알라딘 책은 반드시 작가 정보가 있어야 한다")
    void getMyBookStore_WithAladinBook_ShouldHaveAuthor() {
        // Given: 테스트 회원 생성
        testMember = Member.builder()
                .nickname("테스터3")
                .provider(Member.Provider.KAKAO)
                .providerId("test-provider-id-3")
                .status(Member.Status.ACTIVE)
                .build();
        memberRepository.save(testMember);
        
        // 알라딘 책 + 작가
        Book aladinBook = Book.builder()
                .title("알라딘 책")
                .source(Book.Source.ALADIN)
                .aladinId("12345")
                .isbn("9788901234567")
                .publisher("알라딘 출판사")
                .totalPage(300)
                .build();
        bookRepository.save(aladinBook);

        Writer writer = Writer.builder()
                .writerName("김작가")
                .build();
        writerRepository.save(writer);

        Author author = Author.builder()
                .book(aladinBook)
                .writer(writer)
                .build();
        authorRepository.save(author);

        MyBook myBook = MyBook.builder()
                .member(testMember)
                .book(aladinBook)
                .reason("알라딘에서 찾은 책")
                .readingStatus(MyBook.ReadingStatus.TODO)
                .status(MyBook.Status.ACTIVE)
                .build();
        myBookRepository.save(myBook);

        // 영속성 컨텍스트 강제 플러시 및 클리어
        entityManager.flush();
        entityManager.clear();

        // When
        Pageable pageable = PageRequest.of(0, 5);
        PageRes<MyBookStorePageRes.BookItem> result = myBookService.getMyBookStore(pageable, null, testMember);

        // Then
        assertThat(result.getBooks()).hasSize(1);
        MyBookStorePageRes.BookItem bookItem = result.getBooks().get(0);
        assertThat(bookItem.getBookInfo().getAuthor()).hasSize(1);
        assertThat(bookItem.getBookInfo().getAuthor().get(0)).isEqualTo("김작가");
    }
}
