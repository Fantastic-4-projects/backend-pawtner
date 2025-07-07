package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.service.ProductService;
import com.enigmacamp.pawtner.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> createProduct(@Valid @ModelAttribute ProductRequestDTO productRequestDTO) {
        ProductResponseDTO responseDTO = productService.createProduct(productRequestDTO);
        return ResponseUtil.createResponse(HttpStatus.CREATED, "Successfully created product", responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> getProductById(@PathVariable UUID id) {
        ProductResponseDTO responseDTO = productService.getProductById(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched product", responseDTO);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductResponseDTO>>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> responseDTOPage = productService.getAllProducts(pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all products", responseDTOPage);
    }

    @GetMapping("/my-products/{businessId}")
    public ResponseEntity<CommonResponse<Page<ProductResponseDTO>>> getProductsByBusinessId(@PathVariable UUID businessId, Pageable pageable) {
        Page<ProductResponseDTO> responseDTOPage = productService.getProductsByBusiness(businessId, pageable);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully fetched all products", responseDTOPage);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> updateProduct(@PathVariable UUID id, @Valid @ModelAttribute ProductRequestDTO productRequestDTO) {
        productRequestDTO.setId(id);
        ProductResponseDTO responseDTO = productService.updateProduct(productRequestDTO);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully updated product", responseDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseUtil.createResponse(HttpStatus.OK, "Successfully deleted product", null);
    }
}