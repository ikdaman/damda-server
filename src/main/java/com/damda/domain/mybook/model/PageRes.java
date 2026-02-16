package com.damda.domain.mybook.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * 공통 페이지네이션 응답
 */
@Getter
@Builder
public class PageRes<T> {
    private int totalPages;
    private int nowPage;
    private long totalElements;
    private List<T> books;
}
