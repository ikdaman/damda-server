package com.damda.domain.book.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class SearchResult {
    private Integer aladinId;
    private String title;
    private String author;
    private String publisher;
    private Integer totalPages;
    private String coverImage;
    private String publishDate;
    private String link;
}
