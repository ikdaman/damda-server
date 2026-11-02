package com.damda.domain.collection.model;

import com.damda.domain.collection.entity.Collection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 컬렉션 만들기 DTO (정책서 2-4)
 * - 컬렉션 이름(필수)과, 편집을 마친 최종 책 목록(선택, 최대 9권)을 한 번에 받는다.
 * - books 를 생략하거나 비우면 9칸이 모두 빈 컬렉션이 만들어진다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionReq {

    @NotBlank(message = "컬렉션 이름은 필수입니다.")
    @Size(max = Collection.NAME_MAX_LENGTH, message = "컬렉션 이름은 50자를 초과할 수 없습니다.")
    private String name;

    @Valid
    @Size(max = Collection.MAX_BOOKS, message = "컬렉션에는 최대 9권까지 담을 수 있습니다.")
    private List<CollectionBookReq> books;

    @Builder
    public CollectionReq(String name, List<CollectionBookReq> books) {
        this.name = name;
        this.books = books;
    }
}
