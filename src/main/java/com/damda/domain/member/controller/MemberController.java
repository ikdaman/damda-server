package com.damda.domain.member.controller;

import com.damda.domain.member.model.MemberRes;
import com.damda.domain.member.service.MemberService;
import com.damda.global.auth.model.AuthMember;
import com.damda.global.util.RandomNickname;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity findMyInfo() {
//    public ResponseEntity findMyInfo(@AuthenticationPrincipal AuthMember authMember) {
        MemberRes result = memberService.getMember(null);
//        MemberRes result = memberService.getMember(authMember.getMember().getMemberId());
        return ResponseEntity.ok(result);
    }

    // 닉네임 중복 확인
    @GetMapping("/check")
    public ResponseEntity checkNickname(@RequestParam(name="nickname")
                                        @NotBlank(message = "닉네임은 빈 값일 수 없습니다.")
                                        @Size(min=1, max=9,  message = "닉네임은 최소 1자, 최대 9자만 가능합니다.")
                                        @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$")
                                        String nickname) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", memberService.isNicknameAvailable(nickname));
        return ResponseEntity.ok(result);
    }
}
