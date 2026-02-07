package com.damda.domain.mybook.repository;

import com.damda.domain.book.entity.Book;
import com.damda.domain.member.entity.Member;
import com.damda.domain.mybook.entity.MyBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {
    boolean existsByMemberAndBookAndStatus(Member member, Book Book, MyBook.Status active);
}
