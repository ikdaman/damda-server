package com.damda.domain.member.model;

import com.damda.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 회원 요청 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReq {
    @NotBlank(message = "닉네임은 빈 값일 수 없습니다.")
    @Size(min=1, max=9,  message = "닉네임은 최소 1자, 최대 9자만 가능합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
    private String nickname;

    private Member.Provider provider;

    @Builder
    public MemberReq(String nickname, Member.Provider provider) {
        this.nickname = nickname;
        this.provider = provider;
    }

    public Member toEntity() {
        return Member.builder()
                .nickname(nickname)
                .provider(provider)
                .build();
    }
}
