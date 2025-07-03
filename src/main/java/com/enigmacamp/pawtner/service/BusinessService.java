package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;

import java.util.List;
import java.util.UUID;

import com.enigmacamp.pawtner.entity.Business;

public interface BusinessService {
    BusinessResponseDTO registerBusiness(BusinessRequestDTO businessRequestDTO);
    List<BusinessResponseDTO> viewBusiness();
    List<BusinessResponseDTO> viewMyBusiness();
    Business getBusinessByIdForInternal(UUID id);
    BusinessResponseDTO approveBusiness(UUID businessId, Boolean approved);
}
