package com.damda.global.util;

import com.damda.domain.member.entity.Member;
import com.damda.domain.member.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.damda.domain.mybook.entity.MyBook;
import com.damda.domain.mybook.repository.MyBookRepository;
import com.damda.domain.notification.entity.NotificationLog;
import com.damda.domain.notification.repository.NotificationLogRepository;
import com.damda.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushScheduler {

    private final MemberRepository memberRepository;
    private final MyBookRepository myBookRepository;
    private final NotificationLogRepository notificationLogRepository;

    private final NotificationService notificationService;

    /**
     * 알림 A : 저장 유도
     * - 매일 11:00 KST
     * - 후보 조회는 findCandidatesForPushA(threshold) 사용
     */
    @Scheduled(cron = "0 0 11 * * ?", zone = "Asia/Seoul")
    public void runJobA() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);

        List<Member> candidates = memberRepository.findCandidatesForPushA(threshold);
        log.info("[JobA] candidates={}", candidates.size());

        for (Member member : candidates) {
            try {
                notificationService.sendPushAToMember(member.getMemberId());
            } catch (Exception e) {
                log.warn("[JobA] failed memberId={}", member.getMemberId(), e);
            }
        }
    }

    /**
     * 알림 B: 저장한 책 리마인드
     * - 첫째 금요일 20:00 KST (크론 유지)
     * 기획 조건(notification_log 이용):
     * - pushEnabled=true, ACTIVE (members 조회에서 이미 충족 가능)
     * - TODO 보유자
     * - 오늘 A 성공 발송 이력 없음
     * - 이번 달 B 성공 발송 이력 없음
     */
    @Scheduled(cron = "0 0 20 1-7 * FRI", zone = "Asia/Seoul")
    public void runJobB() {
        List<Member> candidates = memberRepository.findAllPushEnabledActiveMembers();
        log.info("[JobB] candidates(before filter)={}", candidates.size());

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        YearMonth ym = YearMonth.now();
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime nextMonthStart = ym.plusMonths(1).atDay(1).atStartOfDay();

        int attempted = 0;
        int skipped = 0;

        for (Member member : candidates) {
            try {
                // 1) TODO 보유
                boolean hasTodo = myBookRepository.existsByMemberAndReadingStatusAndStatus(
                        member, MyBook.ReadingStatus.TODO, MyBook.Status.ACTIVE
                );
                if (!hasTodo) {
                    skipped++;
                    continue;
                }

                // 2) 오늘 A 성공 발송 이력 없음
                boolean hasASuccessToday = notificationLogRepository.existsSuccessInRange(
                        member.getMemberId(),
                        NotificationLog.Type.A,
                        todayStart,
                        tomorrowStart
                );
                if (hasASuccessToday) {
                    skipped++;
                    continue;
                }

                // 3) 이번 달 B 성공 발송 이력 없음
                boolean hasBSuccessThisMonth = notificationLogRepository.existsSuccessInRange(
                        member.getMemberId(),
                        NotificationLog.Type.B,
                        monthStart,
                        nextMonthStart
                );
                if (hasBSuccessThisMonth) {
                    skipped++;
                    continue;
                }

                attempted++;
                notificationService.sendPushBToMember(member.getMemberId());
            } catch (Exception e) {
                log.warn("[JobB] failed memberId={}", member.getMemberId(), e);
            }
        }

        log.info("[JobB] attempted={}, skipped={}", attempted, skipped);
    }
}