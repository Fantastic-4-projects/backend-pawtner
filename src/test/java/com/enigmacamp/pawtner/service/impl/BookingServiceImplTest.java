package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.constant.DeliveryType;
import com.enigmacamp.pawtner.constant.PaymentStatus;
import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.dto.request.BookingRequestDTO;
import com.enigmacamp.pawtner.dto.response.BookingPriceCalculationResponseDTO;
import com.enigmacamp.pawtner.dto.response.BookingResponseDTO;
import com.enigmacamp.pawtner.entity.Booking;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Payment;
import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.repository.BookingRepository;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.PaymentRepository;
import com.enigmacamp.pawtner.repository.PetRepository;
import com.enigmacamp.pawtner.repository.ServiceRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BusinessService businessService;
    @Mock
    private BusinessRepository businessRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User customer;
    private Pet pet;
    private com.enigmacamp.pawtner.entity.Service service;
    private Business business;
    private BookingRequestDTO bookingRequestDTO;
    private Booking booking;
    private Payment payment;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(UUID.randomUUID())
                .email("customer@example.com")
                .name("Customer Name")
                .role(UserRole.CUSTOMER)
                .build();

        pet = Pet.builder()
                .id(UUID.randomUUID())
                .name("Buddy")
                .owner(customer)
                .build();

        business = Business.builder()
                .id(UUID.randomUUID())
                .name("Pet Clinic")
                .owner(User.builder().id(UUID.randomUUID()).email("owner@example.com").role(UserRole.BUSINESS_OWNER).build())
                .build();

        service = com.enigmacamp.pawtner.entity.Service.builder()
                .id(UUID.randomUUID())
                .name("Grooming")
                .basePrice(BigDecimal.valueOf(50000))
                .business(business)
                .capacityPerDay(10)
                .build();

        bookingRequestDTO = BookingRequestDTO.builder()
                .petId(pet.getId())
                .serviceId(service.getId())
                .startTime(ZonedDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0))
                .endTime(ZonedDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0))
                .deliveryType(DeliveryType.PICKUP)
                .pickupBusinessId(business.getId())
                .build();

        booking = Booking.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .pet(pet)
                .service(service)
                .bookingNumber("BOOK-123")
                .startTime(bookingRequestDTO.getStartTime())
                .endTime(bookingRequestDTO.getEndTime())
                .status(BookingStatus.AWAITING_PAYMENT)
                .totalPrice(BigDecimal.valueOf(50000))
                .deliveryType(DeliveryType.PICKUP)
                .pickupBusiness(business)
                .createdAt(ZonedDateTime.now())
                .build();

        payment = Payment.builder()
                .id(UUID.randomUUID())
                .booking(booking)
                .amount(booking.getTotalPrice())
                .status(PaymentStatus.PENDING)
                .build();
    }

    // =================================== CREATE BOOKING TESTS ===================================
    @Test
    @DisplayName("should create booking successfully for PICKUP delivery type")
    void createBooking_shouldSucceedForPickup() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(businessRepository.findById(any(UUID.class))).thenReturn(Optional.of(business));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);

        BookingResponseDTO response = bookingService.createBooking(bookingRequestDTO, customer.getEmail());

        verify(userRepository, times(1)).findByEmail(customer.getEmail());
        verify(petRepository, times(1)).findById(bookingRequestDTO.getPetId());
        verify(serviceRepository, times(1)).findById(bookingRequestDTO.getServiceId());
        verify(businessRepository, times(1)).findById(bookingRequestDTO.getPickupBusinessId());
        verify(bookingRepository, times(2)).save(any(Booking.class)); // One for initial save, one for updating with payment
        verify(paymentService, times(1)).createPayment(any(Payment.class));

        assertThat(response).isNotNull();
        assertThat(response.getCustomer().getEmail()).isEqualTo(customer.getEmail());
        assertThat(response.getPet().getName()).isEqualTo(pet.getName());
        assertThat(response.getServiceName()).isEqualTo(service.getName());
        assertThat(response.getDeliveryType()).isEqualTo(DeliveryType.PICKUP.name());
        assertThat(response.getBusinessName()).isEqualTo(business.getName());
        assertThat(response.getTotalPrice()).isEqualTo(service.getBasePrice());
        assertThat(response.getStatus()).isEqualTo(BookingStatus.AWAITING_PAYMENT.name());
    }

    @Test
    @DisplayName("should create booking successfully for DELIVERY delivery type")
    void createBooking_shouldSucceedForDelivery() {
        bookingRequestDTO.setDeliveryType(DeliveryType.DELIVERY);
        bookingRequestDTO.setDeliveryLatitude(-6.2088);
        bookingRequestDTO.setDeliveryLongitude(106.8456);
        bookingRequestDTO.setPickupBusinessId(null); // Clear pickup business for delivery

        // Mock business location for distance calculation
        GeometryFactory gf = new GeometryFactory();
        Point businessLocation = gf.createPoint(new Coordinate(106.8450, -6.2080)); // Slightly different location
        businessLocation.setSRID(4326);
        business.setLocation(businessLocation);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5L);
        when(businessRepository.calculateDistanceToBusiness(any(UUID.class), any(Point.class))).thenReturn(1000.0); // 1 km distance
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentService.createPayment(any(Payment.class))).thenReturn(payment);

        BookingResponseDTO response = bookingService.createBooking(bookingRequestDTO, customer.getEmail());

        verify(userRepository, times(1)).findByEmail(customer.getEmail());
        verify(petRepository, times(1)).findById(bookingRequestDTO.getPetId());
        verify(serviceRepository, times(1)).findById(bookingRequestDTO.getServiceId());
        verify(businessRepository, never()).findById(any(UUID.class)); // Should not call for pickup business
        verify(businessRepository, times(1)).calculateDistanceToBusiness(any(UUID.class), any(Point.class));
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(paymentService, times(1)).createPayment(any(Payment.class));

        assertThat(response).isNotNull();
        assertThat(response.getDeliveryType()).isEqualTo(DeliveryType.DELIVERY.name());
        assertThat(response.getDeliveryLatitude()).isEqualTo(bookingRequestDTO.getDeliveryLatitude());
        assertThat(response.getDeliveryLongitude()).isEqualTo(bookingRequestDTO.getDeliveryLongitude());
        assertThat(response.getTotalPrice()).isGreaterThan(service.getBasePrice()); // Should include delivery fee
    }

    @Test
    @DisplayName("should throw ResponseStatusException when user not found during booking creation")
    void createBooking_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, "nonexistent@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");

        verify(petRepository, never()).findById(any(UUID.class));
        verify(serviceRepository, never()).findById(any(UUID.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when pet not found during booking creation")
    void createBooking_shouldThrowException_whenPetNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Pet not found");

        verify(serviceRepository, never()).findById(any(UUID.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when service not found during booking creation")
    void createBooking_shouldThrowException_whenServiceNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Service not found");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when capacity is full")
    void createBooking_shouldThrowException_whenCapacityFull() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(10L); // Capacity is 10

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Kapasitas layanan penuh untuk tanggal yang dipilih.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when pickup business not found for PICKUP delivery type")
    void createBooking_shouldThrowException_whenPickupBusinessNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5L);
        when(businessRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Pickup business not found");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when service does not belong to selected pickup business")
    void createBooking_shouldThrowException_whenServiceDoesNotBelongToPickupBusiness() {
        Business otherBusiness = Business.builder().id(UUID.randomUUID()).name("Other Clinic").build();
        service.setBusiness(otherBusiness); // Service belongs to a different business

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5L);
        when(businessRepository.findById(any(UUID.class))).thenReturn(Optional.of(business)); // Requesting original business

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Service does not belong to the selected pickup business.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when latitude or longitude are missing for DELIVERY delivery type")
    void createBooking_shouldThrowException_whenDeliveryCoordsMissing() {
        bookingRequestDTO.setDeliveryType(DeliveryType.DELIVERY);
        bookingRequestDTO.setDeliveryLatitude(null); // Missing latitude
        bookingRequestDTO.setDeliveryLongitude(106.8456);
        bookingRequestDTO.setPickupBusinessId(null);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(petRepository.findById(any(UUID.class))).thenReturn(Optional.of(pet));
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(bookingRepository.countActiveBookingsForServiceOnDate(any(UUID.class), anyList(), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(5L);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDTO, customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Latitude and longitude are required for delivery.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // =================================== GET BOOKING BY ID TESTS ===================================
    @Test
    @DisplayName("should return BookingResponseDTO when booking found by ID")
    void getBookingById_shouldReturnBookingResponseDTO_whenFound() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.of(booking));

        BookingResponseDTO response = bookingService.getBookingById(booking.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(booking.getId());
        assertThat(response.getBookingNumber()).isEqualTo(booking.getBookingNumber());
    }

    @Test
    @DisplayName("should throw ResponseStatusException when booking not found by ID")
    void getBookingById_shouldThrowException_whenNotFound() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");
    }

    // =================================== GET ALL BOOKINGS TESTS ===================================
    @Test
    @DisplayName("should return all bookings for ADMIN role")
    void getAllBookings_shouldReturnAllBookings_forAdmin() {
        User adminUser = User.builder().id(UUID.randomUUID()).email("admin@example.com").role(UserRole.ADMIN).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Collections.singletonList(booking), pageable, 1);

        when(authentication.getName()).thenReturn(adminUser.getEmail());
        when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(bookingRepository.findAll(pageable)).thenReturn(bookingPage);

        Page<BookingResponseDTO> result = bookingService.getAllBookings(authentication, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("should return bookings by customer for CUSTOMER role")
    void getAllBookings_shouldReturnBookingsByCustomer_forCustomer() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Collections.singletonList(booking), pageable, 1);

        when(authentication.getName()).thenReturn(customer.getEmail());
        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bookingRepository.findByCustomer(customer, pageable)).thenReturn(bookingPage);

        Page<BookingResponseDTO> result = bookingService.getAllBookings(authentication, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("should return bookings by business owner for BUSINESS_OWNER role")
    void getAllBookings_shouldReturnBookingsByBusinessOwner_forBusinessOwner() {
        User ownerUser = business.getOwner();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Collections.singletonList(booking), pageable, 1);

        when(authentication.getName()).thenReturn(ownerUser.getEmail());
        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(businessRepository.findAllByOwner_Id(ownerUser.getId())).thenReturn(Collections.singletonList(business));
        when(serviceRepository.findAllByBusiness(business)).thenReturn(Collections.singletonList(service));
        when(bookingRepository.findByServiceIn(Collections.singletonList(service), pageable)).thenReturn(bookingPage);

        Page<BookingResponseDTO> result = bookingService.getAllBookings(authentication, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("should throw ResponseStatusException when user not found for getAllBookings")
    void getAllBookings_shouldThrowException_whenUserNotFound() {
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getAllBookings(authentication, PageRequest.of(0, 10)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    

    // =================================== GET ALL BOOKINGS BY CUSTOMER TESTS ===================================
    @Test
    @DisplayName("should return bookings by customer email")
    void getAllBookingsByCustomer_shouldReturnBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Collections.singletonList(booking), pageable, 1);

        when(userRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bookingRepository.findByCustomer(customer, pageable)).thenReturn(bookingPage);

        Page<BookingResponseDTO> result = bookingService.getAllBookingsByCustomer(customer.getEmail(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("should throw ResponseStatusException when customer not found for getAllBookingsByCustomer")
    void getAllBookingsByCustomer_shouldThrowException_whenCustomerNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getAllBookingsByCustomer("nonexistent@example.com", PageRequest.of(0, 10)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Customer not found");
    }

    // =================================== GET ALL BOOKINGS BY BUSINESS OWNER TESTS ===================================
    @Test
    @DisplayName("should return bookings by business owner email")
    void getAllBookingsByBusinessOwner_shouldReturnBookings() {
        User ownerUser = business.getOwner();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> bookingPage = new PageImpl<>(Collections.singletonList(booking), pageable, 1);

        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(businessRepository.findAllByOwner_Id(ownerUser.getId())).thenReturn(Collections.singletonList(business));
        when(serviceRepository.findAllByBusiness(business)).thenReturn(Collections.singletonList(service));
        when(bookingRepository.findByServiceIn(Collections.singletonList(service), pageable)).thenReturn(bookingPage);

        Page<BookingResponseDTO> result = bookingService.getAllBookingsByBusinessOwner(ownerUser.getEmail(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(booking.getId());
    }

    @Test
    @DisplayName("should return empty page when business owner not found for getAllBookingsByBusinessOwner")
    void getAllBookingsByBusinessOwner_shouldReturnEmptyPage_whenOwnerNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getAllBookingsByBusinessOwner("nonexistent@example.com", PageRequest.of(0, 10)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Owner not found");
    }

    @Test
    @DisplayName("should return empty page when no businesses found for owner")
    void getAllBookingsByBusinessOwner_shouldReturnEmptyPage_whenNoBusinesses() {
        User ownerUser = business.getOwner();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(businessRepository.findAllByOwner_Id(ownerUser.getId())).thenReturn(Collections.emptyList());

        Page<BookingResponseDTO> result = bookingService.getAllBookingsByBusinessOwner(ownerUser.getEmail(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should return empty page when no services found for businesses")
    void getAllBookingsByBusinessOwner_shouldReturnEmptyPage_whenNoServices() {
        User ownerUser = business.getOwner();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findByEmail(ownerUser.getEmail())).thenReturn(Optional.of(ownerUser));
        when(businessRepository.findAllByOwner_Id(ownerUser.getId())).thenReturn(Collections.singletonList(business));
        when(serviceRepository.findAllByBusiness(business)).thenReturn(Collections.emptyList());

        Page<BookingResponseDTO> result = bookingService.getAllBookingsByBusinessOwner(ownerUser.getEmail(), pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
    }

    // =================================== UPDATE BOOKING STATUS TESTS ===================================
    @Test
    @DisplayName("should update booking status successfully")
    void updateBookingStatus_shouldSucceed() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.of(booking));
        Booking updatedBooking = Booking.builder()
                .id(booking.getId())
                .customer(booking.getCustomer())
                .pet(booking.getPet())
                .service(booking.getService())
                .bookingNumber(booking.getBookingNumber())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(BookingStatus.CONFIRMED)
                .payment(booking.getPayment())
                .deliveryType(booking.getDeliveryType())
                .deliveryLocationType(booking.getDeliveryLocationType())
                .deliveryLatitude(booking.getDeliveryLatitude())
                .deliveryLongitude(booking.getDeliveryLongitude())
                .deliveryAddressDetail(booking.getDeliveryAddressDetail())
                .pickupBusiness(booking.getPickupBusiness())
                .createdAt(booking.getCreatedAt())
                .build();
        when(bookingRepository.save(any(Booking.class))).thenReturn(updatedBooking);

        BookingResponseDTO response = bookingService.updateBookingStatus(booking.getId(), "CONFIRMED");

        verify(bookingRepository, times(1)).findById(booking.getId());
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED.name());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("should throw ResponseStatusException when booking not found for update status")
    void updateBookingStatus_shouldThrowException_whenNotFound() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(UUID.randomUUID(), "CONFIRMED"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // =================================== CANCEL BOOKING TESTS ===================================
    @Test
    @DisplayName("should cancel booking successfully")
    void cancelBooking_shouldSucceed() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.of(booking));
        doNothing().when(bookingRepository).delete(any(Booking.class));

        bookingService.cancelBooking(booking.getId(), customer.getEmail());

        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    @DisplayName("should throw ResponseStatusException when booking not found for cancel")
    void cancelBooking_shouldThrowException_whenNotFound() {
        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(UUID.randomUUID(), customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");

        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when unauthorized to cancel booking")
    void cancelBooking_shouldThrowException_whenUnauthorized() {
        User otherCustomer = User.builder().id(UUID.randomUUID()).email("other@example.com").build();
        booking.setCustomer(otherCustomer); // Booking belongs to another customer

        when(bookingRepository.findById(any(UUID.class))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getId(), customer.getEmail()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are not authorized to cancel this booking");

        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    // =================================== CALCULATE DELIVERY FEE TESTS ===================================
    @Test
    @DisplayName("should calculate delivery fee correctly")
    void calculateDeliveryFee_shouldReturnCorrectFee() {
        double distanceInMeters = 5000.0; // 5 km
        double expectedFee = 10000 + (2000 * 5); // Base fee + 2000 per km
        double actualFee = bookingService.calculateDeliveryFee(distanceInMeters);

        assertThat(actualFee).isEqualTo(expectedFee);
    }

    // =================================== CALCULATE BOOKING PRICE TESTS ===================================
    @Test
    @DisplayName("should calculate booking price correctly")
    void calculateBookingPrice_shouldReturnCorrectPrice() {
        Double latitude = -6.2088;
        Double longitude = 106.8456;
        double distanceInMeters = 1000.0; // 1 km

        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.of(service));
        when(businessRepository.calculateDistanceToBusiness(any(UUID.class), any(Point.class))).thenReturn(distanceInMeters);

        BookingPriceCalculationResponseDTO response = bookingService.calculateBookingPrice(service.getId(), latitude, longitude);

        assertThat(response).isNotNull();
        assertThat(response.getServiceId()).isEqualTo(service.getId());
        assertThat(response.getBasePrice()).isEqualTo(service.getBasePrice());
        assertThat(response.getDeliveryFee()).isEqualByComparingTo(BigDecimal.valueOf(bookingService.calculateDeliveryFee(distanceInMeters)).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(response.getTotalPrice()).isEqualByComparingTo(service.getBasePrice().add(BigDecimal.valueOf(bookingService.calculateDeliveryFee(distanceInMeters))));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when service not found for calculate booking price")
    void calculateBookingPrice_shouldThrowException_whenServiceNotFound() {
        when(serviceRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.calculateBookingPrice(UUID.randomUUID(), -6.2088, 106.8456))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Service not found");
    }

    // =================================== HANDLE WEBHOOK TESTS ===================================
    @Test
    @DisplayName("should update booking status to REQUESTED on capture/settlement webhook")
    void handleWebhook_shouldUpdateStatusToRequested() {
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setPayment(payment);

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", booking.getBookingNumber());
        payload.put("transaction_status", "capture");
        payload.put("fraud_status", "accept");

        when(bookingRepository.findByBookingNumber(anyString())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        bookingService.handleWebhook(payload);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.REQUESTED);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("should update booking status to CANCELLED on cancel/deny/expire webhook")
    void handleWebhook_shouldUpdateStatusToCancelled() {
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setPayment(payment);

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", booking.getBookingNumber());
        payload.put("transaction_status", "cancel");
        payload.put("fraud_status", "accept");

        when(bookingRepository.findByBookingNumber(anyString())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        bookingService.handleWebhook(payload);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.CANCELLED);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("should update booking status to PENDING_PAYMENT on pending webhook")
    void handleWebhook_shouldUpdateStatusToPendingPayment() {
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setPayment(payment);

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", booking.getBookingNumber());
        payload.put("transaction_status", "pending");
        payload.put("fraud_status", "accept");

        when(bookingRepository.findByBookingNumber(anyString())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        bookingService.handleWebhook(payload);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("should not update booking status if new status is same as old status")
    void handleWebhook_shouldNotUpdateStatus_whenStatusIsSame() {
        booking.setStatus(BookingStatus.REQUESTED); // Already REQUESTED
        booking.setPayment(payment);

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", booking.getBookingNumber());
        payload.put("transaction_status", "settlement"); // Will result in REQUESTED
        payload.put("fraud_status", "accept");

        when(bookingRepository.findByBookingNumber(anyString())).thenReturn(Optional.of(booking));

        bookingService.handleWebhook(payload);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("should throw ResponseStatusException when booking not found for webhook")
    void handleWebhook_shouldThrowException_whenBookingNotFound() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", "NONEXISTENT-BOOKING");
        payload.put("transaction_status", "settlement");
        payload.put("fraud_status", "accept");

        when(bookingRepository.findByBookingNumber(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.handleWebhook(payload))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Booking not found");

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
