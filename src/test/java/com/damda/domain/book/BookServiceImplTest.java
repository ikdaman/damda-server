package com.damda.domain.book;

import com.damda.domain.book.model.SearchResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookServiceImplTest {
    private MockRestServiceServer server;
    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://apis.yes24.com/");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new BookServiceImpl(builder.build());
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({
            "ISBN13, 9788925588735, ISBN13",
            "aladinId, 101375755, ItemId"
    })
    void detailUsesYes24SearchType(String searchType, String query, String yes24SearchType) {
        server.expect(requestTo("https://apis.yes24.com/v1/goods/itemDetail"
                        + "?query=" + query + "&searchType=" + yes24SearchType + "&detail=Y"))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "data": {
                            "items": [{
                              "itemId": 101375755,
                              "title": "프로젝트 헤일메리",
                              "author": "앤디 위어",
                              "publisher": "알에이치코리아",
                              "pages": 692,
                              "cover": "https://image.yes24.com/goods/101375755/L",
                              "publishDate": "20260219",
                              "link": "https://www.yes24.com/product/goods/101375755"
                            }]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        SearchResult result = service.searchBook(searchType, query);

        assertThat(result.getAladinId()).isEqualTo(101375755);
        assertThat(result.getTitle()).isEqualTo("프로젝트 헤일메리");
        assertThat(result.getTotalPages()).isEqualTo(692);
        assertThat(result.getPublishDate()).isEqualTo("2026-02-19");
        assertThat(result.getLink()).isEqualTo("https://www.yes24.com/product/goods/101375755");
    }
}
