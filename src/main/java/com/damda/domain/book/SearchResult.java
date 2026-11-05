package com.damda.domain.book;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchResult {
    Long aladinId;
    String title;
    String author;
    String publisher;
    Integer totalPages;
    String coverImage;
    String publishDate;
}
