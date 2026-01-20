package com.damda.domain.member.controller;

import com.damda.domain.member.service.MemberService;
import com.damda.global.auth.model.AuthMember;
import com.damda.global.util.RandomNickname;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 컨트롤러
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final RandomNickname randomNickname;

    /**
     * 회원 탈퇴
     * @param authMember Spring Security에서 인증된 회원 정보
     * @return 205 Reset Content
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> signout(@AuthenticationPrincipal AuthMember authMember) {
        memberService.withdrawMember(authMember.getMember());
        return ResponseEntity.status(HttpStatus.RESET_CONTENT)
                .build();
    }

    /**
     * 닉네임 추천받기
      * @return 사용 가능한 랜덤 닉네임 (String)
     */
    @GetMapping("/suggest-nickname")
    public ResponseEntity<String> suggestNickname() {
        String suggested;
        do {
            suggested = randomNickname.generate();
        } while (!memberService.isNicknameAvailable(suggested));

        return ResponseEntity.ok(suggested);
    }
}
