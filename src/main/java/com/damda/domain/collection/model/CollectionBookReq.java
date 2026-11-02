package com.damda.domain.collection.model;

import com.damda.domain.collection.entity.Collection;
import com.damda.domain.mybook.model.BookInfo;
import com.damda.domain.mybook.model.HistoryInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 컬렉션에 담을 책 한 권 DTO (컬렉션 만들기 / 수정 요청의 books 항목, 정책서 2-5)
 *
 * 두 가지 방식 중 하나로 보낸다.
 *  A) 서가에 이미 있는 책 : mybookId
 *  B) 서가에 없는 책     : bookInfo (+ historyInfo, reason)
 *                          저장 시트에서 입력한 값을 그대로 보내면, 서버가 서가에 저장한 뒤 컬렉션에 담는다.
 *
 * - mybookId 가 있으면 A 로 처리하고 bookInfo 등은 무시한다.
 * - B 로 보냈는데 서버에서 확인해보니 이미 서가에 있는 책(같은 알라딘 ID)이면
 *   historyInfo / reason 은 무시하고 기존 책을 그대로 담는다. (같은 책은 서가에 하나만)
 * - slotNo(1~9)를 보내면 그 칸에 놓는다. 생략하면 서버가 맨 앞의 빈 칸부터 요청 순서대로 채운다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionBookReq {

    /** A) 서가에 이미 있는 책의 나의 책 ID */
    private Long mybookId;

    /** 칸 번호 1~9 (선택) */
    @Min(value = 1, message = "칸 번호는 1 이상이어야 합니다.")
    @Max(value = Collection.MAX_BOOKS, message = "칸 번호는 9 이하여야 합니다.")
    private Integer slotNo;

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
    public CollectionBookReq(Long mybookId, Integer slotNo, BookInfo bookInfo,
                             HistoryInfo historyInfo, String reason) {
        this.mybookId = mybookId;
        this.slotNo = slotNo;
        this.bookInfo = bookInfo;
        this.historyInfo = historyInfo;
        this.reason = reason;
    }
}
