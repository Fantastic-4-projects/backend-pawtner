package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.dto.response.PrescriptionResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PetMapper {
    public static PetResponseDTO mapToResponse(Pet pet){
        List<PrescriptionResponseDTO> prescriptionDTOs;
        if (pet.getPrescriptions() != null) {
            prescriptionDTOs = pet.getPrescriptions().stream()
                    .map(PrescriptionMapper::mapToResponse)
                    .collect(Collectors.toList());
        } else {
            prescriptionDTOs = Collections.emptyList();
        }

        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .age(pet.getAge())
                .gender(pet.getGender())
                .imageUrl(pet.getImageUrl())
                .notes(pet.getNotes())
                .ownerName(pet.getOwner().getName())
                .prescriptions(prescriptionDTOs)
                .build();
    }
}
