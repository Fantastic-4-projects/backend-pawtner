package com.enigmacamp.pawtner.repository;

import java.util.Optional;
import java.util.UUID;
import com.enigmacamp.pawtner.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findAllByOwner_Id(UUID id);

    Optional<Business> findBusinessById(UUID id);
}
