package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.service.PetService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@AllArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<PetResponseDTO>> createPet(@Valid @ModelAttribute PetRequestDTO petRequestDTO, @RequestPart("image") MultipartFile image, Authentication authentication) {
        petRequestDTO.setImage(image);
        PetResponseDTO responseDTO = petService.createPet(petRequestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully created pet", responseDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('BUSINESS_OWNER') or hasAuthority('ADMIN')")
    public ResponseEntity<CommonResponse<PetResponseDTO>> getPetById(@PathVariable UUID id) {
        PetResponseDTO responseDTO = petService.getPetById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched pet", responseDTO);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<Page<PetResponseDTO>>> getMyPets(Authentication authentication, Pageable pageable) {
        Page<PetResponseDTO> responseDTOPage = petService.getAllPetsByOwnerId(authentication.getName(), pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all pets", responseDTOPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<PetResponseDTO>> updatePet(@PathVariable UUID id, @Valid @RequestBody PetRequestDTO petRequestDTO, Authentication authentication) {
        petRequestDTO.setId(id);
        PetResponseDTO responseDTO = petService.updatePet(petRequestDTO, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated pet", responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<CommonResponse<Void>> deletePet(@PathVariable UUID id, Authentication authentication) {
        petService.deletePet(id, authentication.getName());
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully deleted pet", null);
    }
}