package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO getProductById(Integer id);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
    ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO);
    void deleteProduct(Integer id);
}
