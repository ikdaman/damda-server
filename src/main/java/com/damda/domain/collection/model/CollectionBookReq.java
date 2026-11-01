package com.damda.domain.collection.model;

import com.damda.domain.mybook.model.BookInfo;
import com.damda.domain.mybook.model.HistoryInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션에 책 담기 DTO (정책서 2-5 책 담기)
 *
 * 두 가지 방식 중 하나로 요청한다.
 *  A) 서가에 이미 있는 책 : mybookId 만 보낸다. (저장 시트 없이 바로 담김)
 *  B) 서가에 없는 책     : bookInfo (+ historyInfo, reason) 를 보낸다.
 *                          서가에 책을 추가할 때와 똑같이 서가에 저장된 뒤 컬렉션에 담긴다.
 *
 * mybookId 가 있으면 A 로 처리하고 bookInfo 등은 무시한다.
 * B 로 보냈는데 서버에서 확인해보니 이미 서가에 있는 책이면,
 * historyInfo / reason 은 무시하고 기존 책을 그대로 컬렉션에 담는다. (같은 책은 서가에 하나만)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionBookReq {

    /** A) 서가에 이미 있는 책의 나의 책 ID */
    private Long mybookId;

    /** B) 서가에 없는 책의 정보 (외부 도서 정보) */
    @Valid
    private BookInfo bookInfo;

    /** B) 읽는 상태 / 날짜 (없으면 읽고 싶은 책) */
    @Valid
    private HistoryInfo historyInfo;

    /** B) 읽고 싶은 이유 (400자까지, 띠지에 쓰임) */
    @Size(max = 400, message = "읽고 싶은 이유는 400자를 초과할 수 없습니다.")
    private String reason;

    @Builder
    public CollectionBookReq(Long mybookId, BookInfo bookInfo, HistoryInfo historyInfo, String reason) {
        this.mybookId = mybookId;
        this.bookInfo = bookInfo;
        this.historyInfo = historyInfo;
        this.reason = reason;
    }
}
