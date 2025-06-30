package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, Integer> {
    List<Business> findAllByOwner_Id(UUID id);
}
