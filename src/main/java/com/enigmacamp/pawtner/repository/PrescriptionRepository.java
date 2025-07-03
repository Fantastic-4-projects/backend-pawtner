package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
}
