package com.damda.domain.book;

import com.damda.domain.book.model.SearchResult;
import com.damda.domain.mybook.model.PageRes;
import com.damda.global.exception.BaseException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.damda.global.exception.ErrorCode.BAD_REQUEST_INVALID_PAGE_SIZE;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Validated
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
    public ResponseEntity<PageRes<SearchResult>> searchBooks(@RequestParam(required = true) @NotBlank(message = "검색어는 필수 입력값입니다.") String query,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "5") Integer pageSize) {
        if(pageSize <= 0 || pageSize > 100) {
            throw new BaseException(BAD_REQUEST_INVALID_PAGE_SIZE);
        }

        PageRes<SearchResult> res = bookService.searchBooks(query, page, pageSize);

        return ResponseEntity.ok(res);
    }

}
