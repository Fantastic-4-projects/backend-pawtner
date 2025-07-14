package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.config.AuthTokenFilter;
import com.enigmacamp.pawtner.config.JwtService;
import com.enigmacamp.pawtner.constant.ProductCategory;
import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper method to create a mock Authentication object
    private Authentication createMockAuthentication(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                new User(username, "", Collections.singletonList(new SimpleGrantedAuthority(role))),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    @DisplayName("POST /api/products should create a product and return status 201")
    void createProduct_shouldCreateProduct_whenValidDataProvided() throws Exception {
        UUID businessId = UUID.randomUUID();
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .businessId(businessId)
                .category(ProductCategory.FOOD)
                .name("Dog Food")
                .description("Premium dog food")
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(50)
                .build();

        ProductResponseDTO mockResponse = ProductResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Dog Food")
                .category(ProductCategory.FOOD)
                .price(BigDecimal.valueOf(100000))
                .stockQuantity(50)
                .build();

        MockMultipartFile imagePart = new MockMultipartFile("image", "product.jpg", "image/jpeg", "some-image-bytes".getBytes());

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products")
                        .file(imagePart)
                        .param("businessId", requestDTO.getBusinessId().toString())
                        .param("category", requestDTO.getCategory().toString())
                        .param("name", requestDTO.getName())
                        .param("description", requestDTO.getDescription())
                        .param("price", requestDTO.getPrice().toString())
                        .param("stockQuantity", requestDTO.getStockQuantity().toString())
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully created product"))
                .andExpect(jsonPath("$.data.name").value("Dog Food"));

        verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return product by ID and status 200")
    void getProductById_shouldReturnProduct_whenIdExists() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductResponseDTO mockResponse = ProductResponseDTO.builder()
                .id(productId)
                .name("Cat Toy")
                .category(ProductCategory.TOYS)
                .build();

        when(productService.getProductById(productId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched product"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.name").value("Cat Toy"));

        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    @DisplayName("GET /api/products should return all products and status 200")
    void getAllProducts_shouldReturnAllProducts() throws Exception {
        List<ProductResponseDTO> productList = Collections.singletonList(
                ProductResponseDTO.builder().id(UUID.randomUUID()).name("Product 1").build()
        );
        PageImpl<ProductResponseDTO> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productService.getAllProducts(any(Pageable.class), any(), any(), any(), any(), any(), any(), any())).thenReturn(productPage);

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched all products"))
                .andExpect(jsonPath("$.data.content[0].name").value("Product 1"));

        verify(productService, times(1)).getAllProducts(any(Pageable.class), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/products/my-products/{businessId} should return products by business and status 200")
    void getProductsByBusinessId_shouldReturnProducts() throws Exception {
        UUID businessId = UUID.randomUUID();
        List<ProductResponseDTO> productList = Collections.singletonList(
                ProductResponseDTO.builder().id(UUID.randomUUID()).name("Business Product").build()
        );
        PageImpl<ProductResponseDTO> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productService.getProductsByBusiness(eq(businessId), any(), any(), any(), any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/products/my-products/{businessId}", businessId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully fetched products for business"))
                .andExpect(jsonPath("$.data.content[0].name").value("Business Product"));

        verify(productService, times(1)).getProductsByBusiness(eq(businessId), any(), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} should update product and return status 200")
    void updateProduct_shouldUpdateProduct_whenValidDataProvided() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        ProductRequestDTO requestDTO = ProductRequestDTO.builder()
                .id(productId)
                .businessId(businessId)
                .category(ProductCategory.ACCESSORIES)
                .name("Updated Collar")
                .price(BigDecimal.valueOf(50000))
                .stockQuantity(20)
                .build();

        ProductResponseDTO mockResponse = ProductResponseDTO.builder()
                .id(productId)
                .name("Updated Collar")
                .category(ProductCategory.ACCESSORIES)
                .price(BigDecimal.valueOf(50000))
                .stockQuantity(20)
                .build();

        MockMultipartFile imagePart = new MockMultipartFile("image", "updated_product.jpg", "image/jpeg", "some-updated-image-bytes".getBytes());

        when(productService.updateProduct(any(ProductRequestDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/products/{id}", productId)
                        .file(imagePart)
                        .param("id", requestDTO.getId().toString())
                        .param("businessId", requestDTO.getBusinessId().toString())
                        .param("category", requestDTO.getCategory().toString())
                        .param("name", requestDTO.getName())
                        .param("price", requestDTO.getPrice().toString())
                        .param("stockQuantity", requestDTO.getStockQuantity().toString())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully updated product"))
                .andExpect(jsonPath("$.data.name").value("Updated Collar"));

        verify(productService, times(1)).updateProduct(any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} should delete product and return status 200")
    void deleteProduct_shouldDeleteProduct_whenAdmin() throws Exception {
        UUID productId = UUID.randomUUID();

        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId)
                        .principal(createMockAuthentication("owner@example.com", "BUSINESS_OWNER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Successfully deleted product"));

        verify(productService, times(1)).deleteProduct(productId);
    }
}
