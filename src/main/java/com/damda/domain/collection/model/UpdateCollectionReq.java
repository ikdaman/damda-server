package com.damda.domain.collection.model;

import com.damda.domain.collection.entity.Collection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 컬렉션 수정 DTO (정책서 2-5 이름 수정 / 책 담기 / 책 빼기)
 * - 편집 화면에서 '완료'를 눌렀을 때 한 번만 보낸다. (PUT: 전체 교체)
 * - books 는 편집을 마친 최종 책 목록이다. 여기에 없는 책은 컬렉션에서 빠지고(서가의 책은 그대로),
 *   새로 있는 책은 담긴다. 빈 배열이면 모두 뺀다.
 * - books 는 반드시 보내야 한다. (생략을 '전부 비움'으로 오해하지 않도록 필수)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateCollectionReq {

    @NotBlank(message = "컬렉션 이름은 필수입니다.")
    @Size(max = Collection.NAME_MAX_LENGTH, message = "컬렉션 이름은 50자를 초과할 수 없습니다.")
    private String name;

    @NotNull(message = "책 목록은 필수입니다. (비우려면 빈 배열)")
    @Valid
    @Size(max = Collection.MAX_BOOKS, message = "컬렉션에는 최대 9권까지 담을 수 있습니다.")
    private List<CollectionBookReq> books;

    @Builder
    public UpdateCollectionReq(String name, List<CollectionBookReq> books) {
        this.name = name;
        this.books = books;
    }
}
