package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.entity.Booking;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType; // Tambahkan import jika perlu
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingSpecification {
    public static Specification<Booking> getSpecificationByBusiness(
            UUID businessId,
            String bookingNumber,
            String nameCustomer,
            String emailCustomer,
            BookingStatus bookingStatus
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Object, Object> serviceJoin = root.join("service");
            Join<Object, Object> businessJoin = serviceJoin.join("business");
            predicates.add(criteriaBuilder.equal(businessJoin.get("id"), businessId));

            if (bookingNumber != null && !bookingNumber.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bookingNumber")), "%" + bookingNumber.toLowerCase() + "%"));
            }

            Join<Object, Object> customerJoin = root.join("customer", JoinType.INNER);

            if (nameCustomer != null && !nameCustomer.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + nameCustomer.toLowerCase() + "%"));
            }

            if (emailCustomer != null && !emailCustomer.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("email")), "%" + emailCustomer.toLowerCase() + "%"));
            }

            if (bookingStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), bookingStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}