package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.util.ResponseUtil;
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
        businessService.registerBusiness(businessRequestDTO, businessImage, certificateImage);
        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Profil bisnis berhasil dibuat",
                null
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
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> openBusiness(@PathVariable UUID businessId){
        BusinessResponseDTO response = businessService.openBusiness(businessId);
        String message = response.getIsOpen() ? "Bisnis berhasil dibuka" : "Bisnis berhasil ditutup";
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
}
