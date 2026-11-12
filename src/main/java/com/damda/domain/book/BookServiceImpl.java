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

import static com.damda.global.exception.ErrorCode.BAD_REQUEST_WRONG_SEARCH_TYPE;
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
                        .queryParam("sort", "RELATION")
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

    public SearchResult searchBook(String searchType, String query) {
        if(!searchType.equals("ISBN13") && !searchType.equals("aladinId")) {
            throw new BaseException(BAD_REQUEST_WRONG_SEARCH_TYPE);
        }

        YesBookResponse yesBookResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("v1/goods/itemDetail")
                        .queryParam("query", query)
                        .queryParam("searchType", searchType.equals("ISBN13") ? "ISBN13" : "ItemId")
                        .queryParam("detail", "Y")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,(request, res) -> {
                    log.error(request.getURI().toString(), "예스24 API 요청 오류: " + res.getStatusCode(), res.getStatusCode().value());
                    throw new BaseException(OUTER_API_ERROR);
                })
                .body(YesBookResponse.class);

        SearchResult response = SearchResult.builder()
                .aladinId(yesBookResponse.data().items().get(0).itemId())
                .title(yesBookResponse.data().items().get(0).title())
                .author(yesBookResponse.data().items().get(0).author())
                .publisher(yesBookResponse.data().items().get(0).publisher())
                .totalPages(yesBookResponse.data().items().get(0).pages())
                .coverImage(yesBookResponse.data().items().get(0).cover())
                .publishDate(LocalDate.parse(yesBookResponse.data().items().get(0).publishDate(), INPUT).format(OUTPUT))
                .link(yesBookResponse.data().items().get(0).link())
                .build();

        return response;
    }
}
