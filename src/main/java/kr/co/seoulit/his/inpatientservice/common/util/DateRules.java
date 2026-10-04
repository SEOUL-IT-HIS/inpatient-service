package kr.co.seoulit.his.inpatientservice.common.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 날짜 검증의 공통 기준
 * - 병원 기준 시간대(Asia/Seoul)로 "오늘/지금"을 계산
 *   서버 컨테이너가 UTC라 LocalDate.now()를 그대로 쓰면 새벽(KST 00~09시)에 "오늘"이 하루 밀림
 * - 화면(브라우저) 시계와 서버 시계가 조금 어긋나도 "방금 일어난 일"이 미래로 판정되지 않게 5분 여유를 둠
 */
public final class DateRules {

    public static final ZoneId HOSPITAL_ZONE = ZoneId.of("Asia/Seoul");
    private static final Duration CLOCK_SKEW = Duration.ofMinutes(5);

    private DateRules() {
    }

    public static LocalDate today() {
        return LocalDate.now(HOSPITAL_ZONE);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(HOSPITAL_ZONE);
    }

    // 이미 일어난 일(측정·평가·배정·퇴상 등)의 시각으로 쓰기에 미래인지 (시계 오차 여유 포함)
    public static boolean isFuture(LocalDateTime time) {
        return time.isAfter(now().plus(CLOCK_SKEW));
    }
}
