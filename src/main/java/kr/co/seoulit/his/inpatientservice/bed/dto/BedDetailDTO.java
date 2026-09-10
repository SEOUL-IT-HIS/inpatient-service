package kr.co.seoulit.his.inpatientservice.bed.dto;

public record BedDetailDTO(
        String bedId,
        String roomNo,
        String bedNo,
        String bedStatus,
        String patientId
) {
}
