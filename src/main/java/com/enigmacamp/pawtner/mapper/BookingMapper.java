package com.enigmacamp.pawtner.mapper;

import com.enigmacamp.pawtner.constant.DeliveryType;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;


public class BookingMapper {
    public static BookingResponseDTO mapToResponse(Booking booking){
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .customer(UserMapper.mapToResponse(booking.getCustomer()))
                .pet(PetMapper.mapToResponse(booking.getPet()))
                .petName(booking.getPet().getName())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .serviceImageUrl(booking.getService().getImageUrl())
                .businessId(booking.getService().getBusiness().getId())
                .businessName(booking.getService().getBusiness().getName())
                .bookingNumber(booking.getBookingNumber())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .redirectUrl(booking.getPayment() != null ? booking.getPayment().getRedirectUrl() : null)
                .deliveryType(booking.getDeliveryType() != null ? booking.getDeliveryType().name() : null)
                .deliveryAddress(booking.getDeliveryType() == DeliveryType.DELIVERY ? booking.getDeliveryAddressDetail() : null)
                .deliveryLatitude(booking.getDeliveryType() == DeliveryType.DELIVERY ? booking.getDeliveryLatitude() : null)
                .deliveryLongitude(booking.getDeliveryType() == DeliveryType.DELIVERY ? booking.getDeliveryLongitude() : null)
                .businessAddress(booking.getDeliveryType() == DeliveryType.PICKUP && booking.getPickupBusiness() != null ? booking.getPickupBusiness().getAddress() : null)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}