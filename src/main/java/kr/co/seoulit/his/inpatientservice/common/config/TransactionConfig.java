package kr.co.seoulit.his.inpatientservice.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 트랜잭션이 다른 AOP(변경이력 HistoryTrackingAspect)보다 "바깥"에서 시작되도록 순서를 고정
 * - 기본값은 트랜잭션과 Aspect가 둘 다 가장 낮은 우선순위라 어느 쪽이 먼저 감쌀지 보장되지 않음
 * - 트랜잭션이 바깥이어야 Aspect가 @Before에서 저장한 이력이 같은 트랜잭션에 들어가,
 *   본 수정이 실패(검증 예외 등)하면 이력도 함께 롤백됨
 * - proxyTargetClass = true: Spring Boot 기본(CGLIB 프록시)과 동일하게 유지
 */
@Configuration
@EnableTransactionManagement(order = 0, proxyTargetClass = true)
public class TransactionConfig {
}
