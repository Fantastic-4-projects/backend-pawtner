package com.enigmacamp.pawtner.specification;

import com.enigmacamp.pawtner.constant.BookingStatus;
import com.enigmacamp.pawtner.entity.Booking;
import jakarta.persistence.criteria.Join;
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

            Join<Object, Object> customerJoin = root.join("customer");

            predicates.add(criteriaBuilder.equal(root.get("business").get("id"), businessId));

            if (bookingNumber != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("bookingNumber")), "%" + bookingNumber + "%"));
            }

            if (nameCustomer != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("nameCustomer")), nameCustomer));
            }

            if (emailCustomer != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("email")), emailCustomer));
            }

            if (bookingStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("bookingStatus"), bookingStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
