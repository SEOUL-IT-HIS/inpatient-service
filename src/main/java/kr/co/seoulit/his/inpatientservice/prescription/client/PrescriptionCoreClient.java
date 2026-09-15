package kr.co.seoulit.his.inpatientservice.prescription.client;

import kr.co.seoulit.his.inpatientservice.prescription.dto.OutpatientApiResponse;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionCreateDTO;
import kr.co.seoulit.his.inpatientservice.prescription.dto.PrescriptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PrescriptionCoreClient {

    private final RestClient outpatientRestClient;

    public PrescriptionDTO createPrescription(String admissionId, PrescriptionCreateDTO requestDto) {
        return outpatientRestClient.post()
                .uri("/api/outpatient/prescriptions/admission/{admissionId}", admissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<OutpatientApiResponse<PrescriptionDTO>>(){})
                .data();
    }
}
