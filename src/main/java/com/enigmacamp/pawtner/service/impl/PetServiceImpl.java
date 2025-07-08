package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.PetMapper;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.service.ImageUploadService;
import com.enigmacamp.pawtner.service.PetService;
import com.enigmacamp.pawtner.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserService userService;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public PetResponseDTO createPet(PetRequestDTO petRequestDTO, String ownerEmail) {
        User owner = userService.getUserByEmailForInternal(ownerEmail);

        String imageUrl = null;
        if (petRequestDTO.getImage() != null && !petRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(petRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        Pet pet = Pet.builder()
                .owner(owner)
                .name(petRequestDTO.getName())
                .species(petRequestDTO.getSpecies())
                .breed(petRequestDTO.getBreed())
                .age(petRequestDTO.getAge())
                .gender(petRequestDTO.getGender())
                .imageUrl(imageUrl)
                .notes(petRequestDTO.getNotes())
                .build();
        petRepository.save(pet);
        return PetMapper.mapToResponse(pet);
    }

    @Override
    public PetResponseDTO getPetById(UUID id) {
        Pet pet = getPetEntityById(id);
        return PetMapper.mapToResponse(pet);
    }

    @Override
    public Pet getPetEntityById(UUID id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));
    }

    @Override
    public Page<PetResponseDTO> getAllPetsByOwner(String ownerEmail, Pageable pageable) {
        User owner = userService.getUserByEmailForInternal(ownerEmail);
        Page<Pet> pets = petRepository.findByOwner(owner, pageable);
        return pets.map(PetMapper::mapToResponse);
    }

    @Override
    @Transactional
    public PetResponseDTO updatePet(PetRequestDTO petRequestDTO, String ownerEmail) {
        User owner = userService.getUserByEmailForInternal(ownerEmail);
        Pet existingPet = getPetEntityById(petRequestDTO.getId());

        if (!existingPet.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pet does not belong to the authenticated user");
        }

        String imageUrl = existingPet.getImageUrl();
        if (petRequestDTO.getImage() != null && !petRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(petRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        existingPet.setName(petRequestDTO.getName());
        existingPet.setSpecies(petRequestDTO.getSpecies());
        existingPet.setBreed(petRequestDTO.getBreed());
        existingPet.setAge(petRequestDTO.getAge());
        existingPet.setGender(petRequestDTO.getGender());
        existingPet.setImageUrl(imageUrl);
        existingPet.setNotes(petRequestDTO.getNotes());

        petRepository.save(existingPet);
        return PetMapper.mapToResponse(existingPet);
    }

    @Override
    @Transactional
    public void deletePet(UUID id, String ownerEmail) {
        User owner = userService.getUserByEmailForInternal(ownerEmail);
        Pet pet = getPetEntityById(id);

        if (!pet.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pet does not belong to the authenticated user");
        }
        petRepository.delete(pet);
    }
}