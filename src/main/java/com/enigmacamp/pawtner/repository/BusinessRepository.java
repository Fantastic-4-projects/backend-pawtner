package com.enigmacamp.pawtner.repository;

import java.util.Optional;
import java.util.UUID;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findAllByOwner_Id(UUID id);
<<<<<<< HEAD
=======

    Optional<Business> findBusinessById(UUID id);

    Optional<Business> findByOwner(User owner);
>>>>>>> dev/rifqi
}
