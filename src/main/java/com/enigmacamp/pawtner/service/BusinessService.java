package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ApproveBusinessRequestDTO;
import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;

import java.util.List;
import java.util.UUID;

import com.enigmacamp.pawtner.entity.Business;

import org.springframework.web.multipart.MultipartFile;

public interface BusinessService {
    BusinessResponseDTO registerBusiness(BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage);
    BusinessResponseDTO profileBusiness(UUID businessId);
    BusinessResponseDTO updateBusiness(UUID businessId, BusinessRequestDTO businessRequestDTO, MultipartFile businessImage, MultipartFile certificateImage);
    List<BusinessResponseDTO> viewBusiness();
    List<BusinessResponseDTO> viewMyBusiness();
    List<BusinessResponseDTO> findNearbyBusinesses(double lat, double lon, double radiusKm, Boolean hasEmergencyServices, String statusRealtime);
    Business getBusinessByIdForInternal(UUID id);
    BusinessResponseDTO approveBusiness(UUID businessId, ApproveBusinessRequestDTO approved);
    Business getBusinessByOwnerEmailForInternal(String ownerEmail);
    BusinessResponseDTO openBusiness(UUID businessId,  BusinessRequestDTO businessRequestDTO);
    void deleteBusiness(UUID businessId);
}
