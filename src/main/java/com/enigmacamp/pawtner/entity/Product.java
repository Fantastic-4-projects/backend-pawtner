package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.ProductCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false, columnDefinition = "uuid")
    private Business business;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Lob
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(0)
    private Integer stockQuantity;

    @Builder.Default
    private Boolean isActive = true;

    @Size(max = 255)
    private String imageUrl;

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}