package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> createBusiness(
            @RequestPart(required = false, name = "business") BusinessRequestDTO businessRequestDTO,
            @RequestPart(required = false, name = "businessImage") MultipartFile businessImage,
            @RequestPart(required = false, name = "certificateImage") MultipartFile certificateImage
    ) {
        BusinessResponseDTO businessResponseDTO = businessService.registerBusiness(businessRequestDTO, businessImage, certificateImage);
        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Profil bisnis berhasil dibuat",
                businessResponseDTO
        );
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> getBusinessById(@PathVariable UUID businessId) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Profil bisnis didapatkan",
                businessService.profileBusiness(businessId)
        );
    }

    @PutMapping("/{businessId}/update")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> updateBusiness(
            @PathVariable UUID businessId,
            @RequestPart(required = false, name = "business") BusinessRequestDTO businessRequestDTO,
            @RequestPart(required = false, name = "businessImage") MultipartFile businessImage,
            @RequestPart(required = false, name = "certificateImage") MultipartFile certificateImage
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Berhasil memperbaharui profil bisnis",
                businessService.updateBusiness(businessId, businessRequestDTO, businessImage, certificateImage)
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> approveBusiness(
            @PathVariable UUID id, @RequestBody Map<String, Boolean> body
        ){
        Boolean approve = body.get("approve");
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Berhasil mengganti status bisnis.",
                businessService.approveBusiness(id, approve)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<List<BusinessResponseDTO>>> viewBusiness(){
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Profil bisnis didapatkan",
                businessService.viewBusiness()
        );
    }

    @GetMapping("/my-business")
    public ResponseEntity<CommonResponse<List<BusinessResponseDTO>>> viewMyBusiness(){
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Profil bisnis didapatkan",
                businessService.viewMyBusiness()
        );
    }

    @PatchMapping("/{businessId}/toggle-open")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> openBusiness(@PathVariable UUID businessId, @RequestBody BusinessRequestDTO businessRequestDTO) {
        BusinessResponseDTO response = businessService.openBusiness(businessId,  businessRequestDTO);
        String message = "Status realtime berubah menjadi " + response.getStatusRealTime().name();
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                message,
                response
        );
    }

    @DeleteMapping("/{businessId}/delete")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> deleteBusiness(@PathVariable UUID businessId){
        businessService.deleteBusiness(businessId);
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Bisnis berhasil dihapus",
                null
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<CommonResponse<List<BusinessResponseDTO>>> getNearbyBusinesses(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "15") double radiusKm
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Bisnis terdekat berhasil didapatkan",
                businessService.findNearbyBusinesses(lat, lon, radiusKm, null, null)
        );
    }

    @GetMapping("/nearby/emergency")
    public ResponseEntity<CommonResponse<List<BusinessResponseDTO>>> getNearbyEmergencyBusinesses(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "15") double radiusKm
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Bisnis darurat terdekat berhasil didapatkan",
                businessService.findNearbyBusinesses(lat, lon, radiusKm, true, "ACCEPTING_PATIENTS")
        );
    }
}
