package com.damda.domain.mybook.service;

import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.mybook.repository.MyBookRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.damda.domain.mybook.model.MyBookStoreRes;
import com.damda.domain.mybook.model.MyBookHistoryRes;
import com.damda.domain.mybook.model.MyBookSearchRes;
import com.damda.domain.member.entity.Member;

@Service
@RequiredArgsConstructor
public class MyBookServiceImpl implements MyBookService {

    private final MyBookRepository myBookRepository;

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
}
