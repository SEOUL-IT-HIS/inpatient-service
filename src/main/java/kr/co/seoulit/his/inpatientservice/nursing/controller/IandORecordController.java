package kr.co.seoulit.his.inpatientservice.nursing.controller;

import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;

import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordDTO;
import kr.co.seoulit.his.inpatientservice.nursing.dto.IandORecordHistoryDTO;
import kr.co.seoulit.his.inpatientservice.nursing.service.IandORecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inpatient/nursingrecord/iandorecord")
public class IandORecordController {
    private final IandORecordService iandORecordService;

    public IandORecordController(IandORecordService iandORecordService) {
        this.iandORecordService = iandORecordService;
    }

    @GetMapping("/{iandORecordId}/history")
    public ApiResponse<List<IandORecordHistoryDTO>> getIandORecordHistory(@PathVariable String iandORecordId) {
        return ApiResponse.success(iandORecordService.getIandORecordHistory(iandORecordId));
    }

    @GetMapping
    public ApiResponse<List<IandORecordDTO>> getIandORecords() {
        return ApiResponse.success(iandORecordService.getIandORecords());
    }

    @GetMapping("/{iandORecordId}")
    public ApiResponse<IandORecordDTO> getIandORecord(@PathVariable String iandORecordId) {
        return ApiResponse.success(iandORecordService.getIandORecord(iandORecordId));
    }

    @PostMapping
    public ApiResponse<IandORecordDTO> createIandORecord(@RequestBody IandORecordDTO requestDto) {
        return ApiResponse.success(iandORecordService.createIandORecord(requestDto));
    }

    @PutMapping("/{iandORecordId}")
    public ApiResponse<IandORecordDTO> updateIandORecord(@PathVariable String iandORecordId,
                                                                     @RequestBody IandORecordDTO requestDto) {
        return ApiResponse.success(iandORecordService.updateIandORecord(iandORecordId, requestDto));
    }

    @DeleteMapping("/{iandORecordId}")
    public ApiResponse<Void> deleteIandORecord(@PathVariable String iandORecordId) {
        iandORecordService.deleteIandORecord(iandORecordId);
        return ApiResponse.success(null);
    }
}
