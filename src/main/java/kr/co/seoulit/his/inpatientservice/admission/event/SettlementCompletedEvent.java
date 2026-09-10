package kr.co.seoulit.his.inpatientservice.admission.event;

public record SettlementCompletedEvent(
        String admissionId
) {}