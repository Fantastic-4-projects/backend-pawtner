package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.constant.ServiceCategory;
import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Service;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceSpecification {
    public static Specification<Service> getSpecification(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Point userLocation, Double radiusInMeters, UUID businessId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Service, Business> businessJoin = root.join("business");

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
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

    public static Specification<Service> getSpecificationByBusiness(
            UUID businessId, String name, ServiceCategory serviceCategory
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            predicates.add(criteriaBuilder.equal(root.get("business").get("id"), businessId));

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name.toLowerCase() + "%"));
            }

            if (serviceCategory != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceCategory"), serviceCategory));
            }

            return  criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}