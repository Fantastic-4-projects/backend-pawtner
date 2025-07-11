package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Prescription;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    @Query("SELECT p FROM Prescription p WHERE p.refillDate = :date")
    List<Prescription> findAllByRefillDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM Prescription p WHERE p.pet.owner = :owner")
    Page<Prescription> findByPetOwner(@Param("owner") User owner, Pageable pageable);

    Page<Prescription> findByIssuingBusinessIn(List<Business> businesses, Pageable pageable);

    Optional<Prescription> findByBookingId(UUID bookingId);
}
