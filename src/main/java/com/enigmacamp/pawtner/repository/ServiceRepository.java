package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID>, JpaSpecificationExecutor<Service> {
    List<Service> findAllByBusiness(Business business);
}