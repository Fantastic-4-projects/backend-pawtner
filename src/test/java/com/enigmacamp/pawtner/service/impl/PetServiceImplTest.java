package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.PetGender;
import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.PetMapper;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.service.ImageUploadService;
import com.enigmacamp.pawtner.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceImplTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private PetServiceImpl petService;

    private User owner;
    private Pet pet;
    private PetRequestDTO petRequestDTO;
    private PetResponseDTO petResponseDTO;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(UUID.randomUUID())
                .email("owner@example.com")
                .build();

        pet = Pet.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(3)
                .gender(PetGender.MALE)
                .imageUrl("http://example.com/buddy.jpg")
                .notes("Friendly dog")
                .build();

        petRequestDTO = PetRequestDTO.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(3)
                .gender(PetGender.MALE)
                .image(new MockMultipartFile("image", "buddy.jpg", "image/jpeg", "some-image-data".getBytes()))
                .notes("Friendly dog")
                .build();

        petResponseDTO = PetMapper.mapToResponse(pet);
    }

    @Test
    void createPet_Success_WithImage() throws IOException {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(imageUploadService.upload(any())).thenReturn("http://example.com/new-buddy.jpg");
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDTO result = petService.createPet(petRequestDTO, owner.getEmail());

        assertNotNull(result);
        assertEquals(pet.getName(), result.getName());
        assertEquals("http://example.com/new-buddy.jpg", result.getImageUrl());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(imageUploadService, times(1)).upload(petRequestDTO.getImage());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void createPet_Success_WithoutImage() throws IOException {
        petRequestDTO.setImage(null);
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDTO result = petService.createPet(petRequestDTO, owner.getEmail());

        assertNotNull(result);
        assertEquals(pet.getName(), result.getName());
        assertNull(result.getImageUrl());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(imageUploadService, never()).upload(any());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void createPet_ImageUploadFails() throws IOException {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(imageUploadService.upload(any())).thenThrow(new IOException("Upload failed"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.createPet(petRequestDTO, owner.getEmail()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to upload image", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(imageUploadService, times(1)).upload(petRequestDTO.getImage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void getPetById_Success() {
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));

        PetResponseDTO result = petService.getPetById(pet.getId());

        assertNotNull(result);
        assertEquals(pet.getId(), result.getId());
        verify(petRepository, times(1)).findById(pet.getId());
    }

    @Test
    void getPetById_NotFound() {
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.getPetById(UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found", exception.getReason());
        verify(petRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void getPetEntityById_Success() {
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));

        Pet result = petService.getPetEntityById(pet.getId());

        assertNotNull(result);
        assertEquals(pet.getId(), result.getId());
        verify(petRepository, times(1)).findById(pet.getId());
    }

    @Test
    void getPetEntityById_NotFound() {
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.getPetEntityById(UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found", exception.getReason());
        verify(petRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void getAllPetsByOwner_Success() {
        List<Pet> pets = Arrays.asList(pet, Pet.builder().id(UUID.randomUUID()).owner(owner).name("Max").build());
        Page<Pet> petPage = new PageImpl<>(pets, PageRequest.of(0, 10), pets.size());

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(petPage);

        Pageable pageable = PageRequest.of(0, 10);
        Page<PetResponseDTO> result = petService.getAllPetsByOwner(owner.getEmail(), pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Buddy", result.getContent().get(0).getName());
        assertEquals("Max", result.getContent().get(1).getName());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void updatePet_Success_WithNewImage() throws IOException {
        petRequestDTO.setId(pet.getId());
        petRequestDTO.setName("New Buddy Name");
        petRequestDTO.setImage(new MockMultipartFile("image", "new-image.jpg", "image/jpeg", "new-image-data".getBytes()));

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(imageUploadService.upload(any())).thenReturn("http://example.com/new-image.jpg");
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDTO result = petService.updatePet(petRequestDTO, owner.getEmail());

        assertNotNull(result);
        assertEquals("New Buddy Name", result.getName());
        assertEquals("http://example.com/new-image.jpg", result.getImageUrl());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(imageUploadService, times(1)).upload(petRequestDTO.getImage());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void updatePet_Success_WithoutNewImage_KeepOld() throws IOException {
        petRequestDTO.setId(pet.getId());
        petRequestDTO.setName("New Buddy Name");
        petRequestDTO.setImage(null); // No new image

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDTO result = petService.updatePet(petRequestDTO, owner.getEmail());

        assertNotNull(result);
        assertEquals("New Buddy Name", result.getName());
        assertEquals("http://example.com/buddy.jpg", result.getImageUrl()); // Old image URL should remain
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(imageUploadService, never()).upload(any());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void updatePet_Success_DeleteImage() throws IOException {
        petRequestDTO.setId(pet.getId());
        petRequestDTO.setName("New Buddy Name");
        petRequestDTO.setImage(null);
        petRequestDTO.setDeleteImage(true);

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        PetResponseDTO result = petService.updatePet(petRequestDTO, owner.getEmail());

        assertNotNull(result);
        assertEquals("New Buddy Name", result.getName());
        assertNull(result.getImageUrl()); // Image URL should be null
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(imageUploadService, never()).upload(any());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void updatePet_NotFound() throws IOException {
        petRequestDTO.setId(UUID.randomUUID()); // Non-existent ID
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.updatePet(petRequestDTO, owner.getEmail()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(any(UUID.class));
        verify(imageUploadService, never()).upload(any());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void updatePet_Forbidden_PetDoesNotBelongToUser() throws IOException {
        User anotherOwner = User.builder().id(UUID.randomUUID()).email("another@example.com").build();
        petRequestDTO.setId(pet.getId());

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(anotherOwner); // Authenticated user is different
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet)); // Pet belongs to 'owner'

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.updatePet(petRequestDTO, anotherOwner.getEmail()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Pet does not belong to the authenticated user", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(anotherOwner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(imageUploadService, never()).upload(any());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void updatePet_ImageUploadFails() throws IOException {
        petRequestDTO.setId(pet.getId());
        petRequestDTO.setImage(new MockMultipartFile("image", "new-image.jpg", "image/jpeg", "new-image-data".getBytes()));

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(imageUploadService.upload(any())).thenThrow(new IOException("Upload failed"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.updatePet(petRequestDTO, owner.getEmail()));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to upload image", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(imageUploadService, times(1)).upload(petRequestDTO.getImage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void deletePet_Success() {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet); // Mock save for isActive change

        petService.deletePet(pet.getId(), owner.getEmail());

        assertFalse(pet.getIsActive()); // Verify isActive is set to false
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(petRepository, times(1)).save(pet);
    }

    @Test
    void deletePet_NotFound() {
        when(userService.getUserByEmailForInternal(anyString())).thenReturn(owner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.deletePet(UUID.randomUUID(), owner.getEmail()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Pet not found", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(owner.getEmail());
        verify(petRepository, times(1)).findById(any(UUID.class));
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void deletePet_Forbidden_PetDoesNotBelongToUser() {
        User anotherOwner = User.builder().id(UUID.randomUUID()).email("another@example.com").build();

        when(userService.getUserByEmailForInternal(anyString())).thenReturn(anotherOwner);
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                petService.deletePet(pet.getId(), anotherOwner.getEmail()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Pet does not belong to the authenticated user", exception.getReason());
        verify(userService, times(1)).getUserByEmailForInternal(anotherOwner.getEmail());
        verify(petRepository, times(1)).findById(pet.getId());
        verify(petRepository, never()).save(any(Pet.class));
    }
}