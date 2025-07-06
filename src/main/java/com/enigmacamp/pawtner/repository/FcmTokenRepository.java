package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.entity.FcmToken;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, String> {
    List<FcmToken> findByUser(User user);
}
