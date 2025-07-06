package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO getProductById(UUID id);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO);
    void deleteProduct(UUID id);
    Product getProductEntityById(UUID id);
}