package com.damda.domain.book.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class SearchResult {
    private Integer aladinId;
    private String title;
    private String author;
    private String publisher;
    private Integer totalPages;
    private String coverImage;
    private String publishDate;
}
