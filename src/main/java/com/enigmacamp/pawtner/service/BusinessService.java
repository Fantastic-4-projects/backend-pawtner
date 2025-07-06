package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;

import java.util.List;
import java.util.UUID;

import com.enigmacamp.pawtner.entity.Business;

import org.springframework.web.multipart.MultipartFile;

public interface BusinessService {
    void registerBusiness(BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage);
    List<BusinessResponseDTO> viewBusiness();
    List<BusinessResponseDTO> viewMyBusiness();
    Business getBusinessByIdForInternal(UUID id);
    BusinessResponseDTO approveBusiness(UUID businessId, Boolean approved);
    Business getBusinessByOwnerEmailForInternal(String ownerEmail);
}
