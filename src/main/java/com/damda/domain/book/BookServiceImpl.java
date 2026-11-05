package com.damda.domain.book;

import com.damda.domain.mybook.model.PageRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl {
    public PageRes<SearchResult> searchBooks(String query, Integer page, Integer pageSize) {
        // TODO: Implement book search logic
        return null;
    }
}
