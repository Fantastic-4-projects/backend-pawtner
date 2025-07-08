package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;

public class PetMapper {
    public static PetResponseDTO mapToResponse(Pet pet){
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
                .build();
    }
}
