package com.damda.domain.mybook.controller;

import com.damda.domain.mybook.model.MyBookReq;
import com.damda.domain.mybook.model.MyBookRes;
import com.damda.domain.mybook.service.MyBookService;
import com.damda.global.auth.model.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mybooks")
@RequiredArgsConstructor
public class MyBookController {

    private final MyBookService myBookService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MyBookRes> addMyBook(
            @RequestBody MyBookReq dto,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        MyBookRes myBookRes = myBookService.addMyBook(authMember.getMember().getMemberId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{mybook_id}")
    public ResponseEntity<Void> deleteMyBook(@PathVariable Integer mybook_id,
                                             @AuthenticationPrincipal AuthMember authMember) {
        myBookService.deleteMyBook(authMember.getMember().getMemberId(), mybook_id);
        return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
    }
}
