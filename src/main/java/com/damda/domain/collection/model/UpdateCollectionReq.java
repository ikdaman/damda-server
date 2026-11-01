package com.damda.domain.collection.model;

import com.damda.domain.collection.entity.Collection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션 이름 수정 DTO (정책서 2-5 이름 수정)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateCollectionReq {

    @NotBlank(message = "컬렉션 이름은 필수입니다.")
    @Size(max = Collection.NAME_MAX_LENGTH, message = "컬렉션 이름은 50자를 초과할 수 없습니다.")
    private String name;

    @Builder
    public UpdateCollectionReq(String name) {
        this.name = name;
    }
}
