package kr.co.seoulit.his.inpatientservice.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HistoryTrackingAspect {

    @Before("@annotation(tracksHistory)")
    public void recordHistoryBefore(org.aspectj.lang.JoinPoint joinPoint, TracksHistory tracksHistory){
        Object target = joinPoint.getTarget();
        if(target instanceof HistoryTrackable historyTrackable){
            Object[] args = joinPoint.getArgs();
            String id = (String) args[0];
            historyTrackable.recordHistory(id,tracksHistory.changeType());
        }
    }
}
