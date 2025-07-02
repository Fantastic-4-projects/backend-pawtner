package com.enigmacamp.pawtner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PetResponseDTO {
    private Integer id;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String imageUrl;
    private String notes;
    private String ownerName;
}
