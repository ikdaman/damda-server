package com.damda.domain.book;

import com.damda.domain.mybook.model.PageRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    private final BookServiceImpl bookService;

    /**
     * 책 검색
     * @param query 검색어
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 책목록
     */
    @GetMapping("")
    public ResponseEntity<PageRes<SearchResult>> searchBooks(@RequestParam String query,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "5") Integer pageSize) {
        PageRes<SearchResult> res = bookService.searchBooks(query, page, pageSize);

        return ResponseEntity.ok(res);
    }

}
