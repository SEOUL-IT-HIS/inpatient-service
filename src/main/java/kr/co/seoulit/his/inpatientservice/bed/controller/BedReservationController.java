package kr.co.seoulit.his.inpatientservice.bed.controller;

import kr.co.seoulit.his.inpatientservice.bed.dto.BedReservationDTO;
import kr.co.seoulit.his.inpatientservice.bed.service.BedReservationService;
import kr.co.seoulit.his.inpatientservice.common.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bedreservation")
public class BedReservationController {
    private final BedReservationService bedReservationService;

    public BedReservationController(BedReservationService bedReservationService) {
        this.bedReservationService = bedReservationService;
    }

    @GetMapping
    public ApiResponse<List<BedReservationDTO>> getBedReservations() {
        return ApiResponse.success(bedReservationService.getBedReservations());
    }

    @GetMapping("/{bedReservationId}")
    public ApiResponse<BedReservationDTO> getBedReservation(@PathVariable Long bedReservationId) {
        return ApiResponse.success(bedReservationService.getBedReservation(bedReservationId));
    }

    @PostMapping
    public ApiResponse<BedReservationDTO> createBedReservation(@RequestBody BedReservationDTO requestDto) {
        return ApiResponse.success(bedReservationService.createBedReservation(requestDto));
    }

    @PutMapping("/{bedReservationId}")
    public ApiResponse<BedReservationDTO> updateBedReservation(@PathVariable Long bedReservationId,
            @RequestBody BedReservationDTO requestDto) {
        return ApiResponse.success(bedReservationService.updateBedReservation(bedReservationId, requestDto));
    }

    @DeleteMapping("/{bedReservationId}")
    public ApiResponse<Void> deleteBedReservation(@PathVariable Long bedReservationId) {
        bedReservationService.deleteBedReservation(bedReservationId);
        return ApiResponse.success(null);
    }

}
