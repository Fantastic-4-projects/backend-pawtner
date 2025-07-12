package com.enigmacamp.pawtner.dto.response;

import com.enigmacamp.pawtner.constant.PetGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetResponseDTO {
    private UUID id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private PetGender gender;
    private String imageUrl;
    private String notes;
    private String ownerName;
    private List<PrescriptionResponseDTO> prescriptions;
}