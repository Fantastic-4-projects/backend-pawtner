package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Product;
import com.enigmacamp.pawtner.entity.Service;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ServiceSpecification {
    public static Specification<Service> getSpecification(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Point userLocation, Double radiusInMeters
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Service, Business> businessJoin = root.join("business");

            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice));
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
}