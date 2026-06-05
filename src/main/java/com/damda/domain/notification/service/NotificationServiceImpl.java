package com.damda.domain.notification.service;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;
import com.damda.domain.mybook.model.MyBookStorePageRes;
import com.damda.domain.mybook.model.PageRes;
import com.damda.domain.mybook.service.MyBookService;
import com.damda.domain.notification.entity.DeviceToken;
import com.damda.domain.notification.entity.NotificationLog;
import com.damda.domain.notification.enumerate.Platform;
import com.damda.domain.notification.model.SendPushRes;
import com.damda.domain.notification.model.TestPushReq;
import com.damda.domain.notification.repository.DeviceTokenRepository;
import com.damda.domain.notification.repository.NotificationLogRepository;
import com.damda.global.exception.BaseException;
import com.google.firebase.messaging.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.damda.global.exception.ErrorCode.NOT_FOUND_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String TITLE = "모아북";

    private static final List<String> JOB_A_BODIES = List.of(
            "읽고 싶다고 생각한 책, 여기 기록해두세요",
            "오늘 스쳐 지나간 책, 잊기 전에 저장해두세요.",
            "읽겠다고 마음먹은 책, 까먹기 전에 저장해볼까요?"
    );

    private static final List<String> JOB_B_TEMPLATES = List.of(
            "%d일 전에 담아둔 책이 있어요. %s 읽어보셨나요?",
            "%s 읽어보셨나요?",
            "지난 번에 저장한 %s을 읽어보는 건 어떨까요?"
    );

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationLogRepository notificationLogRepository;

    private final MyBookService myBookService;
    private final MemberRepository memberRepository;

    @Override
    public String randomBodyA() {
        return JOB_A_BODIES.get(new Random().nextInt(JOB_A_BODIES.size()));
    }

    /**
     * B 알림 내용
     * - 랜덤으로 추출한 템플릿에 문자가 필요할 경우, 날짜와 제목 정보
     * - 문자가 필요하지 않은 경우, 제목 정보
     */
    @Override
    public String randomBodyB(String bookTitle, int daysAgo) {
        String template = JOB_B_TEMPLATES.get(new Random().nextInt(JOB_B_TEMPLATES.size()));
        if (template.contains("%d")) {
            return String.format(template, daysAgo, bookTitle);
        }
        return String.format(template, bookTitle);
    }

    /**
     * A 알림(저장 유도): "7일간 책 추가/상태 변화 없음"
     * - 대상 후보는 Scheduler에서 memberRepository.findCandidatesForPushA(threshold)로 조회
     * - 여기서는 member 1명에 대해 eligibility 체크 + payload 전송까지 담당
     */
    @Override
    @Transactional
    public void sendPushAToMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        LocalDateTime now = LocalDateTime.now();
        if (!isEligibleForPushA(member, now)) {
            return;
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByMemberId(memberId);
        if (deviceTokens.isEmpty()) {
            // 토큰 없으면 실패 로그 저장
            saveNotificationLog(memberId, NotificationLog.Type.A, NotificationLog.Status.FAILED);
            return;
        }

        // payload 전송 코드
        Map<String, String> data = Map.of("type", "A");
        String body = randomBodyA();

        SendPushRes res = sendToDeviceTokens(deviceTokens, data, body);

        // 알림 전송 후 로그 저장
        saveNotificationLog(
                memberId,
                NotificationLog.Type.A,
                res.isAnySuccess() ? NotificationLog.Status.SUCCESS : NotificationLog.Status.FAILED
        );

        log.info("[SendPushA] memberId={}, anySuccess={}, lastBookActionAt={}", memberId, res.isAnySuccess(), member.getLastBookActionAt());
    }

    /**
     * A 조건 완성
     * - pushEnabled = true
     * - status = ACTIVE
     * - lastBookActionAt == null OR now-7d 이전
     */
    private boolean isEligibleForPushA(Member member, LocalDateTime now) {
        if (member == null) return false;
        if (!member.isPushEnabled()) return false;
        if (member.getStatus() != Member.Status.ACTIVE) return false;

        LocalDateTime last = member.getLastBookActionAt();
        if (last == null) return true;

        LocalDateTime threshold = now.minusDays(7);
        return !last.isAfter(threshold); // last <= threshold
    }

    /**
     * B 알림: TODO(읽고 싶은 책) 중 1권을 랜덤으로 가져와 발송
     * - getMyBookStore()의 반환(PageRes<BookItem>)을 그대로 사용
     */
    @Override
    @Transactional
    public void sendPushBToMember(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(NOT_FOUND_USER));

        if (!member.isPushEnabled() || member.getStatus() != Member.Status.ACTIVE) {
            return;
        }

        // 1) TODO 목록의 전체 개수(totalElements) 파악을 위해 size=1로 한 번 조회
        Pageable firstPage = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRes<MyBookStorePageRes.BookItem> first = myBookService.getMyBookStore(firstPage, null, member);

        // 읽고 싶은 책이 없을 경우 return
        if (first == null || first.getTotalElements() <= 0) {
            return;
        }

        long total = first.getTotalElements();

        // 2) 0 ~ total-1 범위 내에서 랜덤 인덱스 선택
        int randomIndex = (int) (Math.random() * total);

        // 3) size=1이므로 page = randomIndex 로 1권을 가져올 수 있음
        Pageable randomPage = PageRequest.of(randomIndex, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageRes<MyBookStorePageRes.BookItem> page = myBookService.getMyBookStore(randomPage, null, member);

        if (page == null || page.getBooks() == null || page.getBooks().isEmpty()) {
            return;
        }

        MyBookStorePageRes.BookItem item = page.getBooks().get(0);
        if (item.getBookInfo() == null || item.getBookInfo().getTitle() == null || item.getBookInfo().getTitle().isBlank()) {
            return;
        }

        String title = item.getBookInfo().getTitle();

        int daysAgo = 0;
        if (item.getCreatedDate() != null) {
            long diff = ChronoUnit.DAYS.between(item.getCreatedDate(), LocalDateTime.now());
            daysAgo = (int) Math.max(diff, 0);
        }

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByMemberId(memberId);
        if (deviceTokens.isEmpty()) {
            // 토큰 없으면 실패 로그 저장
            saveNotificationLog(memberId, NotificationLog.Type.B, NotificationLog.Status.FAILED);
            return;
        }

        // mybookId는 지금 요구사항이 "title만 보내주자"라 data에 굳이 안 넣음
        // 추후 딥링크에 필요해지면 mybookId 추가 가능
        Map<String, String> data = Map.of("type", "B");
        String body = randomBodyB(title, daysAgo);

        SendPushRes res = sendToDeviceTokens(deviceTokens, data, body);

        // 알림 전송 후 로그 저장
        saveNotificationLog(
                memberId,
                NotificationLog.Type.B,
                res.isAnySuccess() ? NotificationLog.Status.SUCCESS : NotificationLog.Status.FAILED
        );

        log.info("[PushB] memberId={}, anySuccess={}, pickedIndex={}, total={}, title={}",
                memberId, res.isAnySuccess(), randomIndex, total, title);
    }

    /**
     * 테스트 알림 전송
     */
    @Override
    @Transactional
    public SendPushRes sendTestPush(UUID memberId, TestPushReq req) {
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByMemberId(memberId);

        if (deviceTokens.isEmpty()) {
            log.info("[TestPush] No device tokens found for member {}", memberId);
            return new SendPushRes(false);
        }

        Map<String, String> data;
        String body;

        if (req.getType() == TestPushReq.Type.A) {
            data = Map.of("type", "A");
            body = randomBodyA();
        } else {
            data = Map.of("type", "B", "mybookId", "1");
            body = randomBodyB("읽고싶은책", 7);
        }

        return sendToDeviceTokens(deviceTokens, data, body);
    }

    /**
     * 플랫폼별(Android/iOS)로 나눠서 각각 다른 Config을 이용해 전송
     */
    private SendPushRes sendToDeviceTokens(List<DeviceToken> deviceTokens, Map<String, String> data, String body) {
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            return SendPushRes.empty();
        }

        Map<Platform, List<DeviceToken>> grouped = deviceTokens.stream()
                .collect(Collectors.groupingBy(DeviceToken::getPlatform));

        boolean anySuccess = false;

        anySuccess |= sendPlatformMulticast(grouped.getOrDefault(Platform.ANDROID, List.of()), data, body, Platform.ANDROID);
        anySuccess |= sendPlatformMulticast(grouped.getOrDefault(Platform.IOS, List.of()), data, body, Platform.IOS);

        return new SendPushRes(anySuccess);
    }

    /**
     * 플랫폼별 Multicast 전송(500개 chunk)
     * - ANDROID: AndroidConfig 적용
     * - IOS: ApnsConfig 적용(소리 미정 → sound 설정 안 함)
     */
    private boolean sendPlatformMulticast(List<DeviceToken> tokens,
                                          Map<String, String> data,
                                          String body,
                                          Platform platform) {
        if (tokens == null || tokens.isEmpty()) return false;

        List<String> fcmTokens = tokens.stream()
                .map(DeviceToken::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();

        if (fcmTokens.isEmpty()) return false;

        int chunkSize = 500;
        boolean anySuccess = false;

        for (int start = 0; start < fcmTokens.size(); start += chunkSize) {
            List<String> chunk = fcmTokens.subList(start, Math.min(start + chunkSize, fcmTokens.size()));

            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(chunk)
                    .setNotification(Notification.builder()
                            .setTitle(TITLE)
                            .setBody(body)
                            .build())
                    .putAllData(data);

            if (platform == Platform.ANDROID) {
                builder.setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build());
            } else if (platform == Platform.IOS) {
                // 소리 정책 미정 → sound 설정 안 함
                // 필요해지면 .setAps(Aps.builder().setSound("default").build()) 같은 형태로 추가
                builder.setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder().build())
                        .build());
            }

            BatchResponse response;
            try {
                response = firebaseMessaging.sendEachForMulticast(builder.build());
            } catch (FirebaseMessagingException e) {
                log.warn("[FCM] platform={} multicast failed: {}", platform, e.getMessage(), e);
                continue;
            }

            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse r = responses.get(i);
                if (r.isSuccessful()) {
                    anySuccess = true;
                    continue;
                }

                if (isInvalidToken(r.getException())) {
                    String badToken = chunk.get(i);
                    deviceTokenRepository.deleteByFcmToken(badToken);
                }
            }
        }

        return anySuccess;
    }

    private boolean isInvalidToken(FirebaseMessagingException ex) {
        if (ex == null) return false;

        MessagingErrorCode code = ex.getMessagingErrorCode();
        if (code == MessagingErrorCode.UNREGISTERED) return true;

        // INVALID_ARGUMENT는 payload 문제 가능성 → 보수적으로 삭제하지 않음
        return false;
    }

    /**
     * 알림 로그 저장
     */
    private void saveNotificationLog(UUID memberId, NotificationLog.Type type, NotificationLog.Status status) {
        notificationLogRepository.save(NotificationLog.builder()
                .memberId(memberId)
                .type(type)
                .status(status)
                .sentAt(LocalDateTime.now())
                .build());
    }
}
