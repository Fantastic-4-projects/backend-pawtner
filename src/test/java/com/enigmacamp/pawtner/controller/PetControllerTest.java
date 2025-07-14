package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.PetGender;
import com.enigmacamp.pawtner.dto.request.PetRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.PetResponseDTO;
import com.enigmacamp.pawtner.service.PetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a mock Authentication object
    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/pets should create a pet and return status 201")
    void createPet_shouldCreatePet_whenValidDataProvided() throws Exception {
        PetRequestDTO requestDTO = PetRequestDTO.builder()
                .name("Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(3)
                .gender(PetGender.MALE)
                .notes("Friendly dog")
                .build();

        PetResponseDTO mockResponse = PetResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Buddy")
                .species("Dog")
                .build();

        MockMultipartFile petPart = new MockMultipartFile("pet", "", "application/json", objectMapper.writeValueAsBytes(requestDTO));
        MockMultipartFile imagePart = new MockMultipartFile("image", "buddy.jpg", "image/jpeg", "some-image-bytes".getBytes());

        when(petService.createPet(any(PetRequestDTO.class), eq("customer@example.com"))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/pets")
                        .file(petPart)
                        .file(imagePart)
                        .principal(createMockAuthentication("customer@example.com", "CUSTOMER")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully created pet"))
                .andExpect(jsonPath("$.data.name").value("Buddy"));

        verify(petService, times(1)).createPet(any(PetRequestDTO.class), eq("customer@example.com"));
    }

    @Test
    @DisplayName("GET /api/pets/{id} should return pet by ID and status 200")
    void getPetById_shouldReturnPet_whenIdExists() throws Exception {
        UUID petId = UUID.randomUUID();
        PetResponseDTO mockResponse = PetResponseDTO.builder()
                .id(petId)
                .name("Whiskers")
                .species("Cat")
                .build();

        when(petService.getPetById(petId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/pets/{id}", petId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched pet"))
                .andExpect(jsonPath("$.data.id").value(petId.toString()))
                .andExpect(jsonPath("$.data.name").value("Whiskers"));

        verify(petService, times(1)).getPetById(petId);
    }

    @Test
    @DisplayName("GET /api/pets should return all pets for owner and status 200")
    void getMyPets_shouldReturnAllPets() throws Exception {
        List<PetResponseDTO> petList = Collections.singletonList(
                PetResponseDTO.builder().id(UUID.randomUUID()).name("My Pet").build()
        );
        PageImpl<PetResponseDTO> petPage = new PageImpl<>(petList, PageRequest.of(0, 10), 1);

        Authentication mockAuthentication = createMockAuthentication("customer@example.com", "CUSTOMER");

        when(petService.getAllPetsByOwner(eq("customer@example.com"), any(Pageable.class))).thenReturn(petPage);

        mockMvc.perform(get("/api/pets")
                        .principal(mockAuthentication)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all pets"))
                .andExpect(jsonPath("$.data.content[0].name").value("My Pet"));

        verify(petService, times(1)).getAllPetsByOwner(eq("customer@example.com"), any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /api/pets/{id} should update pet and return status 200")
    void updatePet_shouldUpdatePet_whenValidDataProvided() throws Exception {
        UUID petId = UUID.randomUUID();
        PetRequestDTO requestDTO = PetRequestDTO.builder()
                .id(petId)
                .name("Updated Buddy")
                .species("Dog")
                .breed("Golden Retriever")
                .age(4)
                .gender(PetGender.MALE)
                .notes("Updated notes")
                .build();

        PetResponseDTO mockResponse = PetResponseDTO.builder()
                .id(petId)
                .name("Updated Buddy")
                .species("Dog")
                .build();

        MockMultipartFile petPart = new MockMultipartFile("pet", "", "application/json", objectMapper.writeValueAsBytes(requestDTO));
        MockMultipartFile imagePart = new MockMultipartFile("image", "updated_buddy.jpg", "image/jpeg", "some-updated-image-bytes".getBytes());

        when(petService.updatePet(any(PetRequestDTO.class), eq("customer@example.com"))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/pets/{id}", petId)
                        .file(petPart)
                        .file(imagePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .principal(createMockAuthentication("customer@example.com", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated pet"))
                .andExpect(jsonPath("$.data.name").value("Updated Buddy"));

        verify(petService, times(1)).updatePet(any(PetRequestDTO.class), eq("customer@example.com"));
    }

    @Test
    @DisplayName("DELETE /api/pets/{id} should delete pet and return status 200")
    void deletePet_shouldDeletePet_whenAuthenticated() throws Exception {
        UUID petId = UUID.randomUUID();

        doNothing().when(petService).deletePet(eq(petId), eq("customer@example.com"));

        mockMvc.perform(delete("/api/pets/{id}", petId)
                        .principal(createMockAuthentication("customer@example.com", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully deleted pet"));

        verify(petService, times(1)).deletePet(eq(petId), eq("customer@example.com"));
    }
}
