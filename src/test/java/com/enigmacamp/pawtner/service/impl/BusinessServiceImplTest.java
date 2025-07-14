package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.BusinessStatus;
import com.enigmacamp.pawtner.constant.BusinessType;
import com.enigmacamp.pawtner.dto.request.ApproveBusinessRequestDTO;
import com.enigmacamp.pawtner.dto.request.BusinessRequestDTO;
import com.enigmacamp.pawtner.dto.request.OperationHoursDTO;
import com.enigmacamp.pawtner.dto.response.BusinessResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.User;
import com.enigmacamp.pawtner.mapper.BusinessMapper;
import com.enigmacamp.pawtner.repository.BusinessRepository;
import com.enigmacamp.pawtner.repository.UserRepository;
import com.enigmacamp.pawtner.service.EmailService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BusinessServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        // Mock SecurityContextHolder for authenticated user
        User currentUser = User.builder().id(UUID.randomUUID()).name("testuser").email("test@example.com").build();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerBusiness_Success() throws IOException {
        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder()
                .nameBusiness("Test Business")
                .descriptionBusiness("Description")
                .businessAddress("Address")
                .businessType(BusinessType.VETERINARY_CLINIC)
                .hasEmergencyServices(true)
                .businessEmail("business@example.com")
                .businessPhone("1234567890")
                .emergencyPhone("0987654321")
                .businessStatus(BusinessStatus.ACCEPTING_PATIENTS)
                .latitude(BigDecimal.valueOf(10.0))
                .longitude(BigDecimal.valueOf(20.0))
                .operationHours(OperationHoursDTO.builder().build())
                .build();

        MultipartFile businessImage = mock(MultipartFile.class);
        MultipartFile certificateImage = mock(MultipartFile.class);

        when(businessImage.isEmpty()).thenReturn(false);
        when(certificateImage.isEmpty()).thenReturn(false);
        when(imageUploadService.upload(businessImage)).thenReturn("businessImageUrl");
        when(imageUploadService.upload(certificateImage)).thenReturn("certificateImageUrl");

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business savedBusiness = Business.builder()
                .id(UUID.randomUUID())
                .name("Test Business")
                .owner(currentUser)
                .build();
        when(businessRepository.save(any(Business.class))).thenReturn(savedBusiness);

        BusinessResponseDTO response = businessService.registerBusiness(requestDTO, businessImage, certificateImage);

        assertNotNull(response);
        assertEquals("Test Business", response.getBusinessName());
        verify(businessRepository, times(1)).save(any(Business.class));
        verify(imageUploadService, times(1)).upload(businessImage);
        verify(imageUploadService, times(1)).upload(certificateImage);
    }

    @Test
    void registerBusiness_ImageUploadFails() throws IOException {
        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder()
                .nameBusiness("Test Business")
                .descriptionBusiness("Description")
                .businessAddress("Address")
                .businessType(BusinessType.VETERINARY_CLINIC)
                .hasEmergencyServices(true)
                .businessEmail("business@example.com")
                .businessPhone("1234567890")
                .emergencyPhone("0987654321")
                .businessStatus(BusinessStatus.ACCEPTING_PATIENTS)
                .latitude(BigDecimal.valueOf(10.0))
                .longitude(BigDecimal.valueOf(20.0))
                .operationHours(OperationHoursDTO.builder().build())
                .build();

        MultipartFile businessImage = mock(MultipartFile.class);
        MultipartFile certificateImage = mock(MultipartFile.class);

        when(businessImage.isEmpty()).thenReturn(false);
        when(imageUploadService.upload(businessImage)).thenThrow(new IOException("Upload failed"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.registerBusiness(requestDTO, businessImage, certificateImage));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to upload image", exception.getReason());
        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void profileBusiness_Success() {
        UUID businessId = UUID.randomUUID();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business business = Business.builder().id(businessId).name("Test Business").owner(currentUser).build();
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

        BusinessResponseDTO response = businessService.profileBusiness(businessId);

        assertNotNull(response);
        assertEquals(businessId, response.getBusinessId());
        assertEquals("Test Business", response.getBusinessName());
    }

    @Test
    void profileBusiness_NotFound() {
        UUID businessId = UUID.randomUUID();
        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.profileBusiness(businessId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Business not found", exception.getReason());
    }

    @Test
    void updateBusiness_Success() throws IOException {
        UUID businessId = UUID.randomUUID();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business existingBusiness = Business.builder()
                .id(businessId)
                .name("Old Name")
                .description("Old Description")
                .businessImageUrl("oldBusinessUrl")
                .certificateImageUrl("oldCertificateUrl")
                .location(geometryFactory.createPoint(new Coordinate(10.0, 20.0)))
                .owner(currentUser)
                .build();

        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder()
                .nameBusiness("New Name")
                .descriptionBusiness("New Description")
                .businessAddress("New Address")
                .businessType(BusinessType.GROOMING_SALON)
                .hasEmergencyServices(false)
                .businessEmail("newbusiness@example.com")
                .businessPhone("0987654321")
                .emergencyPhone("1234567890")
                .businessStatus(BusinessStatus.CLOSED)
                .latitude(BigDecimal.valueOf(30.0))
                .longitude(BigDecimal.valueOf(40.0))
                .operationHours(OperationHoursDTO.builder().build())
                .build();

        MultipartFile businessImage = mock(MultipartFile.class);
        MultipartFile certificateImage = mock(MultipartFile.class);

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(existingBusiness));
        when(businessImage.isEmpty()).thenReturn(false);
        when(certificateImage.isEmpty()).thenReturn(false);
        when(imageUploadService.upload(businessImage)).thenReturn("newBusinessImageUrl");
        when(imageUploadService.upload(certificateImage)).thenReturn("newCertificateImageUrl");
        when(businessRepository.save(any(Business.class))).thenReturn(existingBusiness);

        BusinessResponseDTO response = businessService.updateBusiness(businessId, requestDTO, businessImage, certificateImage);

        assertNotNull(response);
        assertEquals("New Name", response.getBusinessName());
        assertEquals("New Description", response.getDescription());
        verify(businessRepository, times(1)).save(existingBusiness);
        verify(imageUploadService, times(1)).upload(businessImage);
        verify(imageUploadService, times(1)).upload(certificateImage);
        assertEquals("newBusinessImageUrl", existingBusiness.getBusinessImageUrl());
        assertEquals("newCertificateImageUrl", existingBusiness.getCertificateImageUrl());
        assertEquals(30.0, existingBusiness.getLocation().getY());
        assertEquals(40.0, existingBusiness.getLocation().getX());
    }

    @Test
    void updateBusiness_NoImageUpload() throws IOException {
        UUID businessId = UUID.randomUUID();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business existingBusiness = Business.builder()
                .id(businessId)
                .name("Old Name")
                .description("Old Description")
                .businessImageUrl("oldBusinessUrl")
                .certificateImageUrl("oldCertificateUrl")
                .location(geometryFactory.createPoint(new Coordinate(10.0, 20.0)))
                .owner(currentUser)
                .build();

        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder()
                .nameBusiness("New Name")
                .descriptionBusiness("New Description")
                .businessAddress("New Address")
                .businessType(BusinessType.GROOMING_SALON)
                .hasEmergencyServices(false)
                .businessEmail("newbusiness@example.com")
                .businessPhone("0987654321")
                .emergencyPhone("1234567890")
                .businessStatus(BusinessStatus.CLOSED)
                .latitude(BigDecimal.valueOf(30.0))
                .longitude(BigDecimal.valueOf(40.0))
                .operationHours(OperationHoursDTO.builder().build())
                .build();

        MultipartFile businessImage = mock(MultipartFile.class);
        MultipartFile certificateImage = mock(MultipartFile.class);

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(existingBusiness));
        when(businessImage.isEmpty()).thenReturn(true); // No new image
        when(certificateImage.isEmpty()).thenReturn(true); // No new image
        when(businessRepository.save(any(Business.class))).thenReturn(existingBusiness);

        BusinessResponseDTO response = businessService.updateBusiness(businessId, requestDTO, businessImage, certificateImage);

        assertNotNull(response);
        assertEquals("New Name", response.getBusinessName());
        assertEquals("oldBusinessUrl", existingBusiness.getBusinessImageUrl()); // Should remain old URL
        assertEquals("oldCertificateUrl", existingBusiness.getCertificateImageUrl()); // Should remain old URL
        verify(imageUploadService, never()).upload(any(MultipartFile.class));
    }

    @Test
    void updateBusiness_ImageUploadFails() throws IOException {
        UUID businessId = UUID.randomUUID();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Business existingBusiness = Business.builder()
                .id(businessId)
                .name("Old Name")
                .description("Old Description")
                .businessImageUrl("oldBusinessUrl")
                .certificateImageUrl("oldCertificateUrl")
                .location(geometryFactory.createPoint(new Coordinate(10.0, 20.0)))
                .owner(currentUser)
                .build();

        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder()
                .nameBusiness("New Name")
                .descriptionBusiness("New Description")
                .businessAddress("New Address")
                .businessType(BusinessType.GROOMING_SALON)
                .hasEmergencyServices(false)
                .businessEmail("newbusiness@example.com")
                .businessPhone("0987654321")
                .emergencyPhone("1234567890")
                .businessStatus(BusinessStatus.CLOSED)
                .latitude(BigDecimal.valueOf(30.0))
                .longitude(BigDecimal.valueOf(40.0))
                .operationHours(OperationHoursDTO.builder().build())
                .build();

        MultipartFile businessImage = mock(MultipartFile.class);
        MultipartFile certificateImage = mock(MultipartFile.class);

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(existingBusiness));
        when(businessImage.isEmpty()).thenReturn(false);
        when(imageUploadService.upload(businessImage)).thenThrow(new IOException("Upload failed"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.updateBusiness(businessId, requestDTO, businessImage, certificateImage));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to upload image", exception.getReason());
        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void viewBusiness_Success() {
        List<Business> businesses = Arrays.asList(
                Business.builder().id(UUID.randomUUID()).name("Business 1").owner(User.builder().id(UUID.randomUUID()).email("owner1@example.com").name("Owner 1").build()).build(),
                Business.builder().id(UUID.randomUUID()).name("Business 2").owner(User.builder().id(UUID.randomUUID()).email("owner2@example.com").name("Owner 2").build()).build()
        );
        when(businessRepository.findAll()).thenReturn(businesses);

        List<BusinessResponseDTO> response = businessService.viewBusiness();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Business 1", response.get(0).getBusinessName());
        assertEquals("Business 2", response.get(1).getBusinessName());
    }

    @Test
    void viewMyBusiness_Success() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Business> myBusinesses = Arrays.asList(
                Business.builder().id(UUID.randomUUID()).name("My Business 1").owner(currentUser).isActive(true).build(),
                Business.builder().id(UUID.randomUUID()).name("My Business 2").owner(currentUser).isActive(true).build(),
                Business.builder().id(UUID.randomUUID()).name("Inactive Business").owner(currentUser).isActive(false).build()
        );
        when(businessRepository.findAllByOwner_Id(currentUser.getId())).thenReturn(myBusinesses);

        List<BusinessResponseDTO> response = businessService.viewMyBusiness();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("My Business 1", response.get(0).getBusinessName());
        assertEquals("My Business 2", response.get(1).getBusinessName());
    }

    @Test
    void getBusinessByIdForInternal_Success() {
        UUID businessId = UUID.randomUUID();
        Business business = Business.builder().id(businessId).name("Test Business").build();
        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));

        Business foundBusiness = businessService.getBusinessByIdForInternal(businessId);

        assertNotNull(foundBusiness);
        assertEquals(businessId, foundBusiness.getId());
    }

    @Test
    void getBusinessByIdForInternal_NotFound() {
        UUID businessId = UUID.randomUUID();
        when(businessRepository.findById(businessId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.getBusinessByIdForInternal(businessId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Business not found", exception.getReason());
    }

    @Test
    void approveBusiness_Success() {
        UUID businessId = UUID.randomUUID();
        User owner = User.builder().id(UUID.randomUUID()).email("owner@example.com").name("Owner Name").build();
        Business business = Business.builder().id(businessId).name("Test Business").owner(owner).isApproved(false).build();
        ApproveBusinessRequestDTO approveRequest = ApproveBusinessRequestDTO.builder().approve(true).reason("Approved").build();

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        BusinessResponseDTO response = businessService.approveBusiness(businessId, approveRequest);

        assertNotNull(response);
        assertEquals("Approved", response.getStatusApproved());
        verify(businessRepository, times(1)).save(business);
        verify(emailService, times(1)).sendBusinessApprovalEmail(
                owner.getEmail(), owner.getName(), business.getName(), true, "Approved");
    }

    @Test
    void openBusiness_Success() {
        UUID businessId = UUID.randomUUID();
        Business business = Business.builder().id(businessId).name("Test Business").statusRealtime(BusinessStatus.CLOSED)
                .owner(User.builder().id(UUID.randomUUID()).email("owner@example.com").name("Owner Name").build())
                .build();
        BusinessRequestDTO requestDTO = BusinessRequestDTO.builder().businessStatus(BusinessStatus.ACCEPTING_PATIENTS).build();

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        BusinessResponseDTO response = businessService.openBusiness(businessId, requestDTO);

        assertNotNull(response);
        assertEquals(BusinessStatus.ACCEPTING_PATIENTS, response.getStatusRealTime());
        verify(businessRepository, times(1)).save(business);
    }

    @Test
    void findNearbyBusinesses_Success() {
        double lat = 10.0;
        double lon = 20.0;
        double radiusKm = 5.0;
        Boolean hasEmergencyServices = true;
        String statusRealtime = "ACCEPTING_PATIENTS";

        Point userLocation = geometryFactory.createPoint(new Coordinate(lon, lat));
        userLocation.setSRID(4326);

        List<Business> nearbyBusinesses = Arrays.asList(
                Business.builder().id(UUID.randomUUID()).name("Nearby Business 1").owner(User.builder().id(UUID.randomUUID()).name("Owner Name").email("owner1@example.com").build()).build(),
                Business.builder().id(UUID.randomUUID()).name("Nearby Business 2").owner(User.builder().id(UUID.randomUUID()).name("Owner Name").email("owner2@example.com").build()).build()
        );

        when(businessRepository.findNearbyBusinessesWithFilters(
                any(Point.class), eq(radiusKm * 1000), eq(hasEmergencyServices), eq(statusRealtime)))
                .thenReturn(nearbyBusinesses);

        List<BusinessResponseDTO> response = businessService.findNearbyBusinesses(lat, lon, radiusKm, hasEmergencyServices, statusRealtime);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("Nearby Business 1", response.get(0).getBusinessName());
        assertEquals("Nearby Business 2", response.get(1).getBusinessName());

        ArgumentCaptor<Point> pointCaptor = ArgumentCaptor.forClass(Point.class);
        verify(businessRepository).findNearbyBusinessesWithFilters(pointCaptor.capture(), anyDouble(), anyBoolean(), anyString());
        Point capturedPoint = pointCaptor.getValue();
        assertEquals(lon, capturedPoint.getX());
        assertEquals(lat, capturedPoint.getY());
    }

    @Test
    void deleteBusiness_Success() {
        UUID businessId = UUID.randomUUID();
        Business business = Business.builder().id(businessId).name("Test Business").isActive(true)
                .owner(User.builder().id(UUID.randomUUID()).email("owner@example.com").name("Owner Name").build())
                .build();

        when(businessRepository.findById(businessId)).thenReturn(Optional.of(business));
        when(businessRepository.save(any(Business.class))).thenReturn(business);

        businessService.deleteBusiness(businessId);

        assertFalse(business.getIsActive());
        verify(businessRepository, times(1)).save(business);
    }

    @Test
    void getBusinessByOwnerEmailForInternal_Success() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder().id(UUID.randomUUID()).email(ownerEmail).build();
        Business business = Business.builder().id(UUID.randomUUID()).name("Owner's Business").owner(owner).build();

        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(businessRepository.findByOwner(owner)).thenReturn(Optional.of(business));

        Business foundBusiness = businessService.getBusinessByOwnerEmailForInternal(ownerEmail);

        assertNotNull(foundBusiness);
        assertEquals("Owner's Business", foundBusiness.getName());
    }

    @Test
    void getBusinessByOwnerEmailForInternal_UserNotFound() {
        String ownerEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.getBusinessByOwnerEmailForInternal(ownerEmail));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getBusinessByOwnerEmailForInternal_BusinessNotFoundForOwner() {
        String ownerEmail = "owner@example.com";
        User owner = User.builder().id(UUID.randomUUID()).email(ownerEmail).build();

        when(userRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(owner));
        when(businessRepository.findByOwner(owner)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                businessService.getBusinessByOwnerEmailForInternal(ownerEmail));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Business not found for this owner", exception.getReason());
    }
}
