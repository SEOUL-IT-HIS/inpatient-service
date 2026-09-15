package kr.co.seoulit.his.inpatientservice.prescription.dto;

public record OutpatientApiResponse<T>(String code,String message, T data) {
}
