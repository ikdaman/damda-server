package com.damda.domain.collection.entity;

import com.damda.domain.member.entity.Member;
import com.damda.global.common.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 컬렉션 (주제 서가의 한 쪽)
 * - 3열 3행, 9칸의 틀을 처음부터 가진다. (정책서 2-1)
 * - 컬렉션 하나에 최대 9권까지 담을 수 있다.
 *
 * ※ java.util.Collection 과 이름이 같으므로, 이 클래스를 쓰는 파일에서는
 *    java.util.* 와일드카드 import 를 쓰지 않는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Collection extends BaseTime {

    /** 한 컬렉션에 담을 수 있는 최대 권수 (3열 x 3행) */
    public static final int MAX_BOOKS = 9;

    /** 컬렉션 이름 최대 길이 (정책서에 명시 없음 - 확인 필요) */
    public static final int NAME_MAX_LENGTH = 50;

    @Id
    @GeneratedValue
    @Column(name = "collection_id", nullable = false, updatable = false)
    private Long collectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Builder
    public Collection(Long collectionId, Member member, String name) {
        this.collectionId = collectionId;
        this.member = member;
        this.name = name;
    }

    /**
     * 컬렉션 이름 수정 (정책서 2-5 이름 수정)
     */
    public void updateName(String name) {
        this.name = name;
    }
}
