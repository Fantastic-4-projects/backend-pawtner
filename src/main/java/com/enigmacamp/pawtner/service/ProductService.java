package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.constant.ProductCategory;
import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO getProductById(UUID id);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable, String name, BigDecimal minPrice, BigDecimal maxPrice, Double userLat, Double userLon, Double radiusKm, UUID businessId);
    Page<ProductResponseDTO> getProductsByBusiness(UUID businessId, String name, ProductCategory category, Integer stock, Pageable pageable);
    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO);
    void deleteProduct(UUID id);
    Product getProductEntityById(UUID id);
}