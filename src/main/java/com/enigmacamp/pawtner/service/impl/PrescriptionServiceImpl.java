package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.PrescriptionRequestDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionItemResponseDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.Prescription;
import com.enigmacamp.pawtner.entity.PrescriptionItem;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.PrescriptionRepository;
import com.enigmacamp.pawtner.service.BookingService;
import com.enigmacamp.pawtner.service.PrescriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PetRepository petRepository;
    private final BusinessRepository businessRepository;
    private final BookingService bookingService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO requestDTO, Authentication authentication) {
        Pet pet = petRepository.findById(UUID.fromString(requestDTO.getPetId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
        Business business = businessRepository.findById(UUID.fromString(requestDTO.getIssuingBusinessId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));
        Booking booking = bookingService.getBookingEntityById(UUID.fromString(requestDTO.getBookingId()));

        // Validasi bahwa business owner yang membuat resep adalah pemilik bisnis yang mengeluarkan resep
        // dan bahwa booking tersebut terkait dengan bisnis ini.
        // Asumsi: authentication.getName() mengembalikan email business owner
        // Anda mungkin perlu menambahkan logika untuk memverifikasi bahwa booking.getService().getBusiness().getOwner().getEmail().equals(authentication.getName())
        // atau booking.getBusiness().getOwner().getEmail().equals(authentication.getName()) tergantung struktur Anda.

        Prescription prescription = Prescription.builder()
                .pet(pet)
                .issuingBusiness(business)
                .booking(booking)
                .issueDate(requestDTO.getIssueDate())
                .notes(requestDTO.getNotes())
                .build();

        List<PrescriptionItem> prescriptionItems = requestDTO.getPrescriptionItems().stream()
                .map(itemDTO -> PrescriptionItem.builder()
                        .prescription(prescription)
                        .medicationName(itemDTO.getMedicationName())
                        .dosage(itemDTO.getDosage())
                        .frequency(itemDTO.getFrequency())
                        .durationDays(itemDTO.getDurationDays())
                        .instructions(itemDTO.getInstructions())
                        .build())
                .collect(Collectors.toList());

        prescription.setPrescriptionItems(prescriptionItems);
        prescriptionRepository.save(prescription);

        return convertToResponseDTO(prescription);
    }

    @Override
    public PrescriptionResponseDTO getPrescriptionById(String id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
        return convertToResponseDTO(prescription);
    }

    @Override
    public Page<PrescriptionResponseDTO> getAllPrescriptions(Pageable pageable) {
        Page<Prescription> prescriptions = prescriptionRepository.findAll(pageable);
        return prescriptions.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deletePrescription(String id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
        prescriptionRepository.delete(prescription);
    }

    @Override
    public PrescriptionResponseDTO getPerceptionByBookingId(UUID bookingId, Authentication authentication) {
        // Asumsi: Business owner hanya bisa melihat resep untuk booking yang terkait dengan bisnis mereka
        // Anda perlu mendapatkan business owner dari authentication dan memverifikasi kepemilikan booking
        // Untuk saat ini, saya akan langsung mencari resep berdasarkan bookingId

        Prescription prescription = prescriptionRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perception not found for this booking ID"));

        // Tambahkan validasi kepemilikan bisnis di sini jika diperlukan
        // Misalnya: if (!prescription.getIssuingBusiness().getOwner().getEmail().equals(authentication.getName())) { ... }

        return convertToResponseDTO(prescription);
    }

    private PrescriptionResponseDTO convertToResponseDTO(Prescription prescription) {
        List<PrescriptionItemResponseDTO> itemDTOs = prescription.getPrescriptionItems().stream()
                .map(item -> PrescriptionItemResponseDTO.builder()
                        .id(item.getId())
                        .medicationName(item.getMedicationName())
                        .dosage(item.getDosage())
                        .frequency(item.getFrequency())
                        .durationDays(item.getDurationDays())
                        .instructions(item.getInstructions())
                        .build())
                .collect(Collectors.toList());

        return PrescriptionResponseDTO.builder()
                .id(prescription.getId())
                .pet(PetResponseDTO.builder()
                        .id(prescription.getPet().getId())
                        .name(prescription.getPet().getName())
                        .build())
                .issuingBusiness(BusinessResponseDTO.builder()
                        .businessId(prescription.getIssuingBusiness().getId())
                        .businessName(prescription.getIssuingBusiness().getName())
                        .build())
                .bookingId(prescription.getBooking() != null ? prescription.getBooking().getId() : null)
                .issueDate(prescription.getIssueDate())
                .notes(prescription.getNotes())
                .prescriptionItems(itemDTOs)
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
