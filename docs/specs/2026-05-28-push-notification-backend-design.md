# 푸시 알림 백엔드 설계 (damda-server)

- 작성일: 2026-05-28
- 대상 저장소: `damda-server` (Spring Boot 3.4.3 / Java 17)
- 관련 프론트 문서: `moabook-app/docs/specs/2026-05-28-push-notification-frontend-design.md`

## 1. 목적

모아북 앱에서 "이 책 생각나네" 톤의 부드러운 푸시 알림 3종(A/B/C)을 운영하기 위한 백엔드 인프라 구축. 본 문서는 백엔드 책임 범위(A, B 알림)와 토글/디바이스 토큰 API를 정의한다. C 알림은 클라 로컬 알림으로 처리하므로 백엔드 책임 외.

## 2. 알림 종류 (서버 책임)

| 종류 | 트리거 | 빈도 | 채널 |
| --- | --- | --- | --- |
| A. 저장 유도 | `member.last_book_action_at <= now() - 7d` | 발동 조건마다, 월 최대 2회. B 발동 후 7일간 미발동 | FCM |
| B. 저장한 책 리마인드 | 매월 첫째 금요일 20:00 KST, TODO 보유자 | 월 1회 고정. A가 같은 날 발동했다면 다음날로 미룸 | FCM |
| C. 재방문 유도 | (백엔드 책임 외 — 클라 로컬 알림) | — | — |

## 3. 데이터 모델

### 3.1 `member` 테이블 컬럼 추가

```sql
ALTER TABLE member ADD COLUMN push_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE member ADD COLUMN last_book_action_at TIMESTAMP;
```

- `push_enabled`: 전체 ON/OFF 토글
- `last_book_action_at`: 책 추가/상태변경/삭제 시 갱신. A 잡 진입 조건의 기준 시점.

### 3.2 `device_token` 신규 테이블

```sql
CREATE TABLE device_token (
    id BIGSERIAL PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES member(member_id),
    fcm_token VARCHAR(255) NOT NULL UNIQUE,
    platform VARCHAR(10) NOT NULL,  -- 'ANDROID' | 'IOS'
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_device_token_member ON device_token(member_id);
```

- 한 유저에 N개 디바이스 가능
- `fcm_token` UNIQUE — 다른 유저가 같은 기기에 로그인 시 기존 레코드 `member_id` 갱신 정책 적용
- FCM 응답에서 무효 토큰(`UNREGISTERED`, `INVALID_ARGUMENT`)은 자동 삭제

### 3.3 `notification_log` 신규 테이블

```sql
CREATE TABLE notification_log (
    id BIGSERIAL PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES member(member_id),
    type CHAR(1) NOT NULL,  -- 'A' | 'B'
    sent_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notification_log_lookup ON notification_log(member_id, type, sent_at DESC);
```

- 쿨다운 계산용. "최근 7일 내 B 발동 여부", "이번 달 A 발동 횟수" 쿼리 기반.

## 4. 도메인 패키지 구조

```
com.damda.domain.notification
├── entity
│   ├── DeviceToken.java
│   └── NotificationLog.java
├── repository
│   ├── DeviceTokenRepository.java
│   └── NotificationLogRepository.java
├── service
│   ├── NotificationService.java       // FCM 발송 + 무효 토큰 정리
│   ├── PushScheduler.java             // @Scheduled A/B 잡
│   └── BookActionEventListener.java   // last_book_action_at 갱신
├── controller
│   └── NotificationController.java
├── model
│   ├── DeviceTokenReq.java
│   ├── PushSettingsReq.java
│   ├── PushSettingsRes.java
│   └── BookActionEvent.java
└── config
    └── FirebaseConfig.java
```

## 5. API

### 5.1 디바이스 토큰 등록

```
POST /notifications/device-token
Authorization: Bearer {jwt}
Body: { "fcmToken": "...", "platform": "ANDROID" | "IOS" }
Response: 200 OK
```

- 동일 `fcmToken`이 다른 `member_id`로 존재하면 해당 레코드의 `member_id`를 현재 유저로 갱신
- 동일 `(member_id, fcmToken)` 존재 시 `updated_at`만 갱신
- 앱 시작 시마다 호출

### 5.2 디바이스 토큰 해제

```
DELETE /notifications/device-token
Authorization: Bearer {jwt}
Body: { "fcmToken": "..." }
Response: 204 No Content
```

- 로그아웃 시 호출
- 푸시 권한 해제 시 호출

### 5.3 푸시 설정 조회

```
GET /notifications/settings
Authorization: Bearer {jwt}
Response: { "pushEnabled": true }
```

### 5.4 푸시 설정 변경

```
PATCH /notifications/settings
Authorization: Bearer {jwt}
Body: { "pushEnabled": false }
Response: { "pushEnabled": false }
```

### 5.5 (P0 전용) 테스트 푸시 발송

```
POST /notifications/test
Authorization: Bearer {jwt}
Body: { "type": "A" | "B" }
Response: 200 OK
```

- 개발 단계에서만 노출. 운영에서는 `Profile=dev` 또는 관리자 권한 가드.

## 6. 책 액션 이벤트 트래킹

`MyBookServiceImpl` 침습 최소화를 위해 Spring `ApplicationEventPublisher` 사용.

### 6.1 이벤트 정의

```java
public record BookActionEvent(UUID memberId) {}
```

### 6.2 발행 지점 (`MyBookServiceImpl`)

| 메서드 | 라인 | 액션 |
| --- | --- | --- |
| `addMyBook` | 159 | 책 추가 |
| `deleteMyBook` | 273 | 책 삭제 (soft) |
| `updateMyBook` | 289 | 책 정보 수정 |
| `updateReadingStatus` | 382 | 읽기 상태 변경 |

각 메서드 끝에 1줄 추가:
```java
eventPublisher.publishEvent(new BookActionEvent(member.getMemberId()));
```

### 6.3 리스너

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onBookAction(BookActionEvent event) {
    memberRepository.updateLastBookActionAt(event.memberId(), LocalDateTime.now());
}
```

- 트랜잭션 커밋 후 실행 → 롤백 시 미반영
- 별도 트랜잭션. 실패해도 본 로직에 영향 없음

## 7. 스케줄러

### 7.1 `@EnableScheduling`

`DamdaApplication` 또는 별도 `@Configuration` 에 추가.

### 7.2 A 잡 — 저장 유도

```java
@Scheduled(cron = "0 0 11 * * ?", zone = "Asia/Seoul")
public void runJobA() {
    List<Member> candidates = memberRepository.findCandidatesForJobA(
        LocalDateTime.now().minusDays(7),
        YearMonth.now().atDay(1).atStartOfDay(),
        LocalDateTime.now().minusDays(7)  // B 쿨다운
    );
    // ... 각 유저에게 FCM 발송 + notification_log 기록
}
```

쿼리 조건:
- `push_enabled = TRUE`
- `last_book_action_at <= NOW() - 7일` OR `last_book_action_at IS NULL`
- 이번 달 A 발동 횟수 < 2
- 최근 7일 내 B 발동 이력 없음

랜덤 문구 3종 중 1개 선택:
- "읽고 싶다고 생각한 책, 여기 기록해두세요"
- "오늘 스쳐 지나간 책, 잊기 전에 저장해두세요."
- "읽겠다고 마음먹은 책, 까먹기 전에 저장해볼까요?"

### 7.3 B 잡 — 저장한 책 리마인드

```java
@Scheduled(cron = "0 0 20 1-7 * FRI", zone = "Asia/Seoul")
public void runJobB() {
    // 매월 1~7일 사이 금요일 20시 = 첫째 금요일
    List<Member> candidates = memberRepository.findCandidatesForJobB(LocalDate.now());
    // ... 각 유저별 TODO 중 ORDER BY RANDOM() LIMIT 1
}
```

쿼리 조건:
- `push_enabled = TRUE`
- TODO 상태 ACTIVE MyBook 1개 이상 보유
- 오늘 A 발동 이력 없음 → 있으면 다음날 20시로 미룸 (별도 cron `0 0 20 2-8 * SAT` 보완)

문구 (랜덤):
- "n일 전에 담아둔 책이 있어요. {book.title} 읽어보셨나요?"
- "{book.title} 읽어보셨나요?"
- "지난 번에 저장한 {book.title} 을 읽어보는 건 어떨까요?"

### 7.4 멀티 인스턴스 대비

현재는 단일 인스턴스 가정. 운영 환경에서 다중 파드 전환 시:
- `net.javacrumbs.shedlock:shedlock-spring` 도입
- `@SchedulerLock(name = "jobA", lockAtLeastFor = "5m", lockAtMostFor = "10m")` 추가
- PostgreSQL JDBC 락 프로바이더 사용 (Redis 락도 가능)

## 8. FCM 통합

### 8.1 의존성

```gradle
implementation 'com.google.firebase:firebase-admin:9.2.0'
```

### 8.2 서비스 계정 키

- `firebase-service-account.json` → `src/main/resources/` (`*.yml`처럼 gitignore)
- 운영: 환경변수 `GOOGLE_APPLICATION_CREDENTIALS` 또는 Spring Cloud Vault

### 8.3 `FirebaseConfig`

```java
@Configuration
public class FirebaseConfig {
    @Bean
    FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }
}
```

### 8.4 발송 패턴

```java
MulticastMessage message = MulticastMessage.builder()
    .addAllTokens(tokens)
    .setNotification(Notification.builder()
        .setTitle("모아북")
        .setBody(body)
        .build())
    .putData("type", "A")  // 또는 B
    .putData("mybookId", String.valueOf(mybookId))  // B만
    .build();

BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

// 무효 토큰 정리
for (int i = 0; i < response.getResponses().size(); i++) {
    SendResponse r = response.getResponses().get(i);
    if (!r.isSuccessful() && isInvalidToken(r.getException())) {
        deviceTokenRepository.deleteByFcmToken(tokens.get(i));
    }
}
```

## 9. 푸시 페이로드 규약

프론트 deep link 라우팅용 `data` 필드:

| 종류 | `type` | 추가 필드 | 프론트 라우팅 |
| --- | --- | --- | --- |
| A | `"A"` | — | `/barcode` (바코드 스캔) |
| B | `"B"` | `mybookId` | `/mybook/{mybookId}` (책 상세) |
| C | (클라 로컬, 서버 미사용) | — | `/home` |

알림 페이로드 형식은 프론트 문서와 일치해야 함.

## 10. 구현 단계

| 단계 | 범위 | 산출물 |
| --- | --- | --- |
| **P0** | FirebaseConfig + `device_token` 테이블/엔티티/등록·해제 API + `push_enabled` 컬럼/토글 API + 테스트 push endpoint | FCM 인프라 검증 |
| **P1** | `BookActionEvent` 발행/리스너 + `last_book_action_at` 트래킹 + A 잡 cron + `notification_log` | 핵심 알림 가동 |
| **P2** | B 잡 cron + 책 랜덤 선택 + A/B 충돌 처리(다음날 보정 cron) | 월 1회 리마인드 |
| **P3** | 무효 토큰 자동 정리 + 운영 로그 + Slack/Sentry 모니터링 | 운영 안정성 |

## 11. 위험과 대응

| 위험 | 대응 |
| --- | --- |
| 서비스 계정 키 유출 | `.gitignore`, 환경변수, Vault. 키 노출 시 Firebase Console에서 즉시 재발급 |
| FCM 토큰 만료/무효 누적 | `MessagingErrorCode.UNREGISTERED` 등 자동 삭제 |
| `@Scheduled` 멀티 인스턴스 중복 발송 | ShedLock 또는 운영 인스턴스 1대로 고정 |
| Hibernate `ddl-auto=update` 한계 | 운영에 Flyway 도입 시 본 변경분을 마이그레이션 스크립트로 |
| 푸시 사일런트 실패 | `notification_log`로 발송 이력 추적, 실패 시 에러 로그 + 다음 잡에서 재시도 가능 (별도 큐 불필요) |

## 12. 미정 / 후속

- ShedLock 사전 도입 여부 (현재: 멀티 인스턴스 운영 시 추가)
- A/B 발송 시간대 조정 (현재 A=11시, B=금요일 20시 — 기획 변경 시 cron만 수정)
- 종류별 토글 (`push_enabled` 외에 `push_a_enabled` 등 분리) — 기획상 필요해지면 컬럼 추가
- 발송 통계 대시보드 (현재: `notification_log` 조회로 대체)
