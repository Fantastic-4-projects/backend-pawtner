package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<BusinessResponseDTO>> createBusiness(
            @Valid @RequestBody BusinessRequestDTO businessRequestDTO
    ) {
        return ResponseUtil.createResponse(
                HttpStatus.CREATED,
                "Profil bisnis berhasil dibuat",
                businessService.registerBusiness(businessRequestDTO)
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
