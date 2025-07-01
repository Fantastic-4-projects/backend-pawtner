package com.enigmacamp.pawtner.controller;

import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.CommonResponse;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> createProduct(@Valid @ModelAttribute ProductRequestDTO productRequestDTO) {
        ProductResponseDTO responseDTO = productService.createProduct(productRequestDTO);
        CommonResponse<ProductResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully created product",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> getProductById(@PathVariable Integer id) {
        ProductResponseDTO responseDTO = productService.getProductById(id);
        CommonResponse<ProductResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully fetched product",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CommonResponse<Page<ProductResponseDTO>>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> responseDTOPage = productService.getAllProducts(pageable);
        CommonResponse<Page<ProductResponseDTO>> commonResponse = new CommonResponse<>(
                "Successfully fetched all products",
                responseDTOPage
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<ProductResponseDTO>> updateProduct(@PathVariable Integer id, @Valid @ModelAttribute ProductRequestDTO productRequestDTO) {
        productRequestDTO.setId(id);
        ProductResponseDTO responseDTO = productService.updateProduct(productRequestDTO);
        CommonResponse<ProductResponseDTO> commonResponse = new CommonResponse<>(
                "Successfully updated product",
                responseDTO
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        CommonResponse<Void> commonResponse = new CommonResponse<>(
                "Successfully deleted product",
                null
        );
        return new ResponseEntity<>(commonResponse, HttpStatus.OK);
    }
}
