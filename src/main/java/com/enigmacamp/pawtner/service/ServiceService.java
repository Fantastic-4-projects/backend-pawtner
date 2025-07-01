package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {
    Service create(Service service);
    Service getById(Integer id);
    Page<Service> getAll(Pageable pageable);
    Service update(Service service);
    void delete(Integer id);
}
