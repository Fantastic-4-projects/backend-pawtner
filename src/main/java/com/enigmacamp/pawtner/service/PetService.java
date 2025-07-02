package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PetService {
    PetResponseDTO createPet(PetRequestDTO petRequestDTO, String ownerEmail);
    PetResponseDTO getPetById(Integer id);
    Pet getPetEntityById(Integer id);
    Page<PetResponseDTO> getAllPetsByOwnerId(String ownerEmail, Pageable pageable);
    PetResponseDTO updatePet(PetRequestDTO petRequestDTO, String ownerEmail);
    void deletePet(Integer id, String ownerEmail);
}
