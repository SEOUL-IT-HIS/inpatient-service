package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BedService {

    List<BedDTO> getBeds();

    BedDTO getBed(@PathVariable String bedId);

    BedDTO updateRoomType(String bedId, String roomTypeCode);

    BedDTO updateWard(String bedId, String wardCd);

    // 청소 완료 — 점검중(MAINTENANCE) 병상만 EMPTY로 되돌림
    BedDTO makeBedAvailable(String bedId);
}
