package com.enigmacamp.pawtner.service;

import com.enigmacamp.pawtner.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Product create(Product product);
    Product getById(Integer id);
    Page<Product> getAll(Pageable pageable);
    Product update(Product product);
    void delete(Integer id);
}
