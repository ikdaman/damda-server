package com.damda.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

// 닉네임 중복검사 -> 가입이 이루어지는 Service 단에서 검사하는 편이 의존성을 줄일 수 있음 (추가적인 Repository를 선언할 필요 없음)
@Component
@RequiredArgsConstructor
public class RandomNickname {

    private static final int MAX_NICKNAME_NUMBER = 999;
    private static final int MIN_NICKNAME_NUMBER = 1;

    private static int ADJ_INDEX = 0;
    private static int N_INDEX = 0;

    // 형용사 배열
    private final String[] ADJECTIVES = {
            "책을 좋아하는", "책에 진심인", "책방을 좋아하는", "책 냄새 좋아하는", "독서하는",
            "책장 넘기는", "깊은 독서하는", "따뜻한", "느긋한", "강한",
            "말랑한", "단단한", "조용한", "여유있는", "깊은",
            "유쾌한", "감성적인", "책 읽는", "몰입하는", "끈기있는",
            "고요한", "사색적인", "숨겨진", "보고 싶은", "재미있는",
            "용기의", "꿈꾸는", "건강한", "행복한", "고마운"
    };

    // 명사 배열
    private final String[] NOUNS = {
            "독서인", "독자", "책친구", "독서가", "독서광",
            "학습가", "독서왕", "책러버", "북러버", "독서부자",
            "리더", "책선생님", "책대장", "사색가", "친구",
            "이웃", "어른", "사람", "감자", "고구마",
            "배추", "당근", "양파", "버섯", "브로콜리",
            "옥수수", "토마토", "양상추", "도토리"
    };

    /**
     * 랜덤 닉네임을 생성하는 메소드
     */
    public String generate() {
        // 랜덤 형용사
        String adjective = ADJECTIVES[ADJ_INDEX];
        // 랜덤 명사
        String noun = NOUNS[N_INDEX];
        // 랜덤 숫자
        int randomInt = (int) (Math.random() * (MAX_NICKNAME_NUMBER - MIN_NICKNAME_NUMBER) + MIN_NICKNAME_NUMBER);

        ADJ_INDEX = (ADJ_INDEX + 1 ) % ADJECTIVES.length;
        N_INDEX = (N_INDEX + 1 ) % NOUNS.length;

        // 생성한 닉네임 반환 (Ex. 책을 좋아하는 독서인 27)
        return MessageFormat.format("{0} {1} {2}", adjective, noun, randomInt);
    }
}

