package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.constant.OrderStatus;
import com.enigmacamp.pawtner.entity.Order;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderSpecification {
    public static Specification<Order> getSpecificationByBusiness(
            UUID businessId,
            String orderNumber,
            String nameCustomer,
            String emailCustomer,
            OrderStatus orderStatus
    ) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("business").get("id"), businessId));

            if (orderNumber != null && !orderNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNumber")), "%" + orderNumber.toLowerCase() + "%"));
            }

            Join<Object, Object> customerJoin = root.join("customer", JoinType.INNER);

            if (nameCustomer != null && !nameCustomer.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + nameCustomer.toLowerCase() + "%"));
            }

            if (emailCustomer != null && !emailCustomer.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("email")), "%" + emailCustomer.toLowerCase() + "%"));
            }

            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), orderStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}