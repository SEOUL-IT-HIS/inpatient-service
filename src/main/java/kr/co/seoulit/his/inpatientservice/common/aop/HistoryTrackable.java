package kr.co.seoulit.his.inpatientservice.common.aop;

public interface HistoryTrackable {
    void recordHistory(String id,String changeType);
}
