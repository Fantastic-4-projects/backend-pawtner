package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ServiceService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;
    private final ServiceService serviceService;

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> createBusiness(
            @Valid @RequestBody BusinessRequestDTO businessRequestDTO
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Profil bisnis berhasil dibuat",
                businessService.registerBusiness(businessRequestDTO)
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
    public ResponseEntity<CommonResponse<List<BusinessResponseDTO>>> viewBusiness(){
        return ResponseUtil.createResponse(
                HttpStatus.OK,
                "Profil bisnis didapatkan",
                businessService.viewBusiness()
        );
    }
}
