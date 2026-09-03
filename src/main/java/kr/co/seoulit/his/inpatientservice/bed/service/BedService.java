package kr.co.seoulit.his.inpatientservice.bed.service;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedDTO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BedService {

    List<BedDTO> getBeds();

    BedDTO getBed(@PathVariable String bedId);
    BedDTO updateRoomType(String bedId, String roomTypeCode);

}
