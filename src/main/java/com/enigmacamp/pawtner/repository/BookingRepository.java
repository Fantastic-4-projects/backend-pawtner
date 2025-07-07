package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.service.id = :serviceId AND b.status IN :statuses AND b.startTime >= :startDate AND b.startTime < :endDate")
    long countActiveBookingsForServiceOnDate(
            @Param("serviceId") UUID serviceId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Page<Booking> findAllByService_Business(Business serviceBusiness, Pageable pageable);
    Optional<Booking> findByBookingNumber(String bookingNumber);
}
