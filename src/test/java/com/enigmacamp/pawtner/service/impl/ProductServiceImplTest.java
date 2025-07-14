package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.ProductCategory;
import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Product;
import com.enigmacamp.pawtner.repository.ProductRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private BusinessService businessService;
    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Business business;
    private ProductRequestDTO productRequestDTO;

    @BeforeEach
    void setUp() {
        business = Business.builder()
                .id(UUID.randomUUID())
                .name("Test Business")
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .name("Dog Food")
                .category(ProductCategory.FOOD)
                .price(new BigDecimal("150.00"))
                .stockQuantity(10)
                .business(business)
                .isActive(true)
                .build();

        productRequestDTO = ProductRequestDTO.builder()
                .businessId(business.getId())
                .name("Dog Food")
                .category(ProductCategory.FOOD)
                .price(new BigDecimal("150.00"))
                .stockQuantity(10)
                .image(new MockMultipartFile("image", "test.jpg", "image/jpeg", "test data".getBytes()))
                .build();
    }

    @Test
    @DisplayName("createProduct should return ProductResponseDTO on success")
    void createProduct_shouldReturnDTO_onSuccess() throws IOException {
        // Given
        when(businessService.getBusinessByIdForInternal(any(UUID.class))).thenReturn(business);
        when(imageUploadService.upload(any())).thenReturn("http://image.url/test.jpg");
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(product.getName());
        verify(businessService).getBusinessByIdForInternal(business.getId());
        verify(imageUploadService).upload(productRequestDTO.getImage());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("getProductById should return ProductResponseDTO when product exists")
    void getProductById_shouldReturnDTO_whenFound() {
        // Given
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        // When
        ProductResponseDTO result = productService.getProductById(product.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(product.getId());
    }

    @Test
    @DisplayName("getProductById should throw ResponseStatusException when product not found")
    void getProductById_shouldThrowException_whenNotFound() {
        // Given
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("getAllProducts should return a page of ProductResponseDTOs")
    void getAllProducts_shouldReturnPageOfDTOs() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product), pageable, 1);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // When
        Page<ProductResponseDTO> result = productService.getAllProducts(pageable, null, null, null, null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Dog Food");
    }

    @Test
    @DisplayName("deleteProduct should set isActive to false")
    void deleteProduct_shouldSetIsActiveToFalse() {
        // Given
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.deleteProduct(product.getId());

        // Then
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("deleteProduct should throw exception when product not found")
    void deleteProduct_shouldThrowException_whenNotFound() {
        // Given
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResponseStatusException.class, () -> productService.deleteProduct(UUID.randomUUID()));
        verify(productRepository, never()).save(any(Product.class));
    }
}