package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.service.NotificationService;
import com.enigmacamp.pawtner.service.SchedulingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import com.enigmacamp.pawtner.entity.Prescription;
import com.enigmacamp.pawtner.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingServiceImpl implements SchedulingService {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final PrescriptionRepository prescriptionRepository;

    @Override
    @Scheduled(cron = "0 * * * * *") // Runs every minute
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(1);

        log.info("Checking for booking reminders between {} and {}", now, reminderTime);

        List<Booking> bookings = bookingRepository.findAllByStatusAndStartTimeBetween(
                BookingStatus.CONFIRMED,
                now,
                reminderTime
        );

        for (Booking booking : bookings) {
            log.info("Sending reminder for booking: {}", booking.getId());
            String title = "Pengingat Pemesanan Anda";
            String body = String.format("Pemesanan %s untuk %s akan dimulai dalam 1 jam di %s.",
                    booking.getService().getName(),
                    booking.getPet().getName(),
                    booking.getService().getBusiness().getName());
            Map<String, String> data = new HashMap<>();
            data.put("type", "BOOKING_REMINDER");
            data.put("id", booking.getId().toString());
            notificationService.sendNotification(booking.getCustomer(), title, body, data);
        }
    }

    @Scheduled(cron = "0 0 8 * * *") // Runs every day at 8 AM
    public void sendPrescriptionReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(3);
        log.info("Checking for prescription reminders for date: {}", reminderDate);

        List<Prescription> prescriptions = prescriptionRepository.findAllByRefillDate(reminderDate);

        for (Prescription prescription : prescriptions) {
            log.info("Sending reminder for prescription: {}", prescription.getId());
            String title = "Pengingat Resep Obat";
            String body = String.format("Resep obat untuk %s akan habis pada %s atau perlu di-refill.",
                    prescription.getPet().getName(),
                    prescription.getRefillDate());
            Map<String, String> data = new HashMap<>();
            data.put("type", "PRESCRIPTION_REMINDER");
            data.put("id", prescription.getId().toString());
            notificationService.sendNotification(prescription.getPet().getOwner(), title, body, data);
        }
    }
}
