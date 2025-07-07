package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Business;
import com.enigmacamp.pawtner.entity.Review;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByUser(User user, Pageable pageable);
    Page<Review> findByBusinessIn(List<Business> businesses, Pageable pageable);
}
