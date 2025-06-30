package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;

import java.util.List;

public interface BusinessService {
    BusinessResponseDTO registerBusiness(BusinessRequestDTO businessRequestDTO);
    List<BusinessResponseDTO> viewBusiness();
}
