package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PetService {
    PetResponseDTO createPet(PetRequestDTO petRequestDTO, String ownerEmail);
    PetResponseDTO getPetById(UUID id);
    Pet getPetEntityById(UUID id);
    Page<PetResponseDTO> getAllPetsByOwnerId(String ownerEmail, Pageable pageable);
    PetResponseDTO updatePet(PetRequestDTO petRequestDTO, String ownerEmail);
    void deletePet(UUID id, String ownerEmail);
}