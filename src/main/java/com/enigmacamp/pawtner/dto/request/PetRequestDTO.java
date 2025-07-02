package com.enigmacamp.pawtner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PetRequestDTO {
    private Integer id;

    @NotBlank(message = "Pet name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Species is required")
    @Size(max = 255)
    private String species;

    @NotBlank(message = "Breed is required")
    @Size(max = 255)
    private String breed;

    @NotNull(message = "Age is required")
    private Integer age;

    @Size(max = 255)
    private MultipartFile image;

    @Size(max = 2000)
    private String notes;
}
