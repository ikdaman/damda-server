package com.damda.domain.mybook.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 책 정보 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookInfo {

    @NotBlank(message = "책 출처는 필수입니다.")
    @Pattern(regexp = "^(ALADIN|CUSTOM)$", message = "출처는 ALADIN 또는 CUSTOM이어야 합니다.")
    private String source;

    private Integer aladinId;

    @Pattern(
            regexp = "^(?:97[89])?\\d{9}[\\dXx]$",
            message = "유효한 ISBN 형식이 아닙니다."
    )
    private String isbn;

    @NotBlank(message = "책 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "저자는 필수입니다.")
    private String author;

    @NotBlank(message = "출판사는 필수입니다.")
    private String publisher;

    private String description;

    @NotNull(message = "총 페이지 수는 필수입니다.")
    private Integer totalPage;

    @NotNull(message = "출판일은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = com.damda.global.util.DateToDateTimeDeserializer.class)
    private LocalDateTime publishDate;

    private String coverImage;

    @Builder
    public BookInfo(String source, Integer aladinId, String isbn, String title,
                    String author, String publisher, String description,
                    Integer totalPage, LocalDateTime publishDate, String coverImage) {
        this.source = source;
        this.aladinId = aladinId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.description = description;
        this.totalPage = totalPage;
        this.publishDate = publishDate;
        this.coverImage = coverImage;
    }
}