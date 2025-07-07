package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    Page<Service> findAllByBusiness(Business business, Pageable pageable);
    List<Service> findAllByBusiness(Business business);
}