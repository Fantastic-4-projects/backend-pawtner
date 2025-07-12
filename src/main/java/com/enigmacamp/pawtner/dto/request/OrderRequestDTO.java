package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.DeliveryLocationType;
import com.enigmacamp.pawtner.constant.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequestDTO {
    private DeliveryType deliveryType;
    private DeliveryLocationType deliveryLocationType;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryAddressDetail;
    private UUID pickupBusinessId;
}
