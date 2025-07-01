package com.enigmacamp.pawtner.repository;

import java.util.UUID;
import com.enigmacamp.pawtner.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findAllByOwner_Id(UUID id);
}
