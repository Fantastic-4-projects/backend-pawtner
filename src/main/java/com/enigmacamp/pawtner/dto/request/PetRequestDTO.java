package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.PetGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetRequestDTO {
    private UUID id;

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

    @NotNull(message = "Gender is required")
    private PetGender gender;

    private MultipartFile image;

    @Size(max = 2000)
    private String notes;

    private Boolean deleteImage;
}