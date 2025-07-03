
package com.enigmacamp.pawtner.entity;

import com.enigmacamp.pawtner.constant.BusinessStatus;
import com.enigmacamp.pawtner.constant.BusinessType;
import com.enigmacamp.pawtner.converter.OperationHoursConverter;
import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
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
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Business type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessType businessType;

    @Builder.Default
    private Boolean hasEmergencyServices = false;

    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String businessEmail;

    @Size(max = 50)
    private String businessPhone;

    @Size(max = 50)
    private String emergencyPhone;

    @Size(max = 255)
    private String businessImageUrl;

    @Size(max = 255)
    private String certificateImageUrl;

    @Column(length = 2000)
    private String address;

    @Digits(integer = 9, fraction = 6)
    private BigDecimal latitude;

    @Digits(integer = 9, fraction = 6)
    private BigDecimal longitude;

    @Convert(converter = OperationHoursConverter.class)
    @Column(columnDefinition = "TEXT")
    private OperationHoursDTO operationHours;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private BusinessStatus statusRealtime = BusinessStatus.CLOSED;

    @Builder.Default
    private Boolean isApproved = null;

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
