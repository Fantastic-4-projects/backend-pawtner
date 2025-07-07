package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Service;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<Booking> findByBookingNumber(String bookingNumber);
    Page<Booking> findByCustomer(User customer, Pageable pageable);
    Page<Booking> findByServiceIn(List<Service> services, Pageable pageable);
}
