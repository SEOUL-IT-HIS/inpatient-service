package kr.co.seoulit.his.inpatientservice.common.dto;

public record AdminApiResponse<T>(int code,String message, T data) {
}
