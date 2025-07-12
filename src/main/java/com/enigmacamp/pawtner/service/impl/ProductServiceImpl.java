package com.enigmacamp.pawtner.service.impl;

import com.enigmacamp.pawtner.constant.ProductCategory;
import com.enigmacamp.pawtner.dto.request.ProductRequestDTO;
import com.enigmacamp.pawtner.dto.response.ProductResponseDTO;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Product;
import com.enigmacamp.pawtner.mapper.ProductMapper;
import com.enigmacamp.pawtner.repository.ProductRepository;
import com.enigmacamp.pawtner.service.BusinessService;
import com.enigmacamp.pawtner.service.ImageUploadService;
import com.enigmacamp.pawtner.service.ProductService;
import com.enigmacamp.pawtner.specification.ProductSpecification;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BusinessService businessService;
    private final ImageUploadService imageUploadService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public Product getProductEntityById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        Business business = businessService.getBusinessByIdForInternal(productRequestDTO.getBusinessId());
        String imageUrl = null;
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(productRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        Product product = Product.builder()
                .business(business)
                .name(productRequestDTO.getName())
                .category(productRequestDTO.getCategory())
                .description(productRequestDTO.getDescription())
                .price(productRequestDTO.getPrice())
                .stockQuantity(productRequestDTO.getStockQuantity())
                .imageUrl(imageUrl)
                .isActive(true)
                .build();
        productRepository.save(product);
        return ProductMapper.mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductMapper.mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable, String name, BigDecimal minPrice, BigDecimal maxPrice, Double userLat, Double userLon, Double radiusKm) {

        Point userLocation = null;
        Double radiusInMeters = null;

        if (userLat != null && userLon != null) {
            userLocation = geometryFactory.createPoint(new Coordinate(userLon, userLat));
            radiusInMeters = (radiusKm != null ? radiusKm : 15.0) * 1000.0;
        }

        Specification<Product> spec = ProductSpecification.getSpecification(
                name,
                minPrice,
                maxPrice,
                userLocation,
                radiusInMeters
        );

        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(ProductMapper::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByBusiness(UUID businessId, String name, ProductCategory category, Integer stock, Pageable pageable) {
        businessService.getBusinessByIdForInternal(businessId);
        Specification<Product> spec = ProductSpecification.getBusinessProductSpecification(businessId, name, category, stock);
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(ProductMapper::mapToResponse);
    }

    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO) {
        Product existingProduct = productRepository.findById(productRequestDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        String imageUrl = existingProduct.getImageUrl();
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            try {
                imageUrl = imageUploadService.upload(productRequestDTO.getImage());
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
            }
        }

        existingProduct.setName(productRequestDTO.getName());
        existingProduct.setCategory(productRequestDTO.getCategory());
        existingProduct.setDescription(productRequestDTO.getDescription());
        existingProduct.setPrice(productRequestDTO.getPrice());
        existingProduct.setStockQuantity(productRequestDTO.getStockQuantity());
        existingProduct.setImageUrl(imageUrl);

        productRepository.save(existingProduct);
        return ProductMapper.mapToResponse(existingProduct);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }
}