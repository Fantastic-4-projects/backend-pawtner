package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, String> {
}
