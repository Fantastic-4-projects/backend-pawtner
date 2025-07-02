package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.Pet;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {
    List<Pet> findByOwner(User owner);
}
