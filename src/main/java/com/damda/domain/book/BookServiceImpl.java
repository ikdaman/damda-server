package com.damda.domain.book;

import com.damda.domain.book.model.SearchResult;
import com.damda.domain.book.model.YesBookResponse;
import com.damda.domain.mybook.model.PageRes;
import com.damda.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.damda.global.exception.ErrorCode.OUTER_API_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl {
    private final RestClient restClient;
    private static final DateTimeFormatter INPUT  = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PageRes<SearchResult> searchBooks(String query, Integer page, Integer pageSize) {
        YesBookResponse yesBookResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/goods/itemList")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("detail", "Y")
                        .queryParam("category", "BOOK")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,(request, res) -> {
                    log.error(request.getURI().toString(), "예스24 API 요청 오류: " + res.getStatusCode(), res.getStatusCode().value());
                    throw new BaseException(OUTER_API_ERROR);
                })
                .body(YesBookResponse.class);

        if(yesBookResponse.data() == null) {
            return null;
        }

        int totalPages = (int) Math.ceil((double) yesBookResponse.data().totalCount() / pageSize);

        PageRes<SearchResult> pageRes = PageRes.<SearchResult>builder()
                .totalPages(totalPages)
                .nowPage(yesBookResponse.data().currentPage())
                .totalElements(yesBookResponse.data().totalCount())
                .books(
                        yesBookResponse.data().items().stream()
                                .map(item -> SearchResult.builder()
                                        .aladinId(item.itemId())
                                        .title(item.title())
                                        .author(item.author())
                                        .publisher(item.publisher())
                                        .totalPages(item.pages())
                                        .coverImage(item.cover())
                                        .publishDate(LocalDate.parse(item.publishDate(), INPUT).format(OUTPUT))
                                        .build()
                                ).toList())
                .build();

        return pageRes;
    }
}
