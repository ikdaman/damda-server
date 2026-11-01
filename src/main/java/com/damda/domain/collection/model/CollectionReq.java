package com.damda.domain.collection.model;

import com.damda.domain.collection.entity.Collection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션 만들기 DTO (정책서 2-4)
 * - 입력 항목은 컬렉션 이름(필수) 하나다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionReq {

    @NotBlank(message = "컬렉션 이름은 필수입니다.")
    @Size(max = Collection.NAME_MAX_LENGTH, message = "컬렉션 이름은 50자를 초과할 수 없습니다.")
    private String name;

    @Builder
    public CollectionReq(String name) {
        this.name = name;
    }
}
