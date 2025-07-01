package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    private final ServiceRepository serviceRepository;

    @Override
    public com.enigmacamp.pawtner.entity.Service create(com.enigmacamp.pawtner.entity.Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public com.enigmacamp.pawtner.entity.Service getById(Integer id) {
        return serviceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
    }

    @Override
    public Page<com.enigmacamp.pawtner.entity.Service> getAll(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }

    @Override
    public com.enigmacamp.pawtner.entity.Service update(com.enigmacamp.pawtner.entity.Service service) {
        com.enigmacamp.pawtner.entity.Service existingService = getById(service.getId());
        return serviceRepository.save(service);
    }

    @Override
    public void delete(Integer id) {
        serviceRepository.deleteById(id);
    }
}
