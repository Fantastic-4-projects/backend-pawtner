package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.constant.ProductCategory;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Product;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductSpecification {
    public static Specification<Product> getSpecification(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Point userLocation, Double radiusInMeters,  UUID businessId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Product, Business> businessJoin = root.join("business");

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (businessId != null) {
                predicates.add(criteriaBuilder.equal(root.get("business").get("id"), businessId));
            }

            if (userLocation != null && radiusInMeters != null) {
                Expression<Boolean> withinExpression = criteriaBuilder.function(
                        "ST_DWithin",
                        Boolean.class,
                        criteriaBuilder.function("geography", Object.class, businessJoin.get("location")),
                        criteriaBuilder.function("geography", Object.class, criteriaBuilder.literal(userLocation)),
                        criteriaBuilder.literal(radiusInMeters)
                );
                predicates.add(criteriaBuilder.isTrue(withinExpression));
            }

            query.groupBy(root.get("id"));

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    public static Specification<Product> getBusinessProductSpecification(
            UUID businessId, String name, ProductCategory category, Integer stock
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("business").get("id"), businessId));

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            if (stock != null) {
                if (stock == 0) {
                    predicates.add(criteriaBuilder.equal(root.get("stockQuantity"), 0));
                } else if (stock == 10) {
                    predicates.add(criteriaBuilder.between(root.get("stockQuantity"), 1, 10));
                } else if (stock > 10) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("stockQuantity"), 10));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}