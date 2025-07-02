package com.enigmacamp.pawtner.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.enigmacamp.pawtner.entity.User;

public interface UserService extends UserDetailsService {
    User getUserByEmailForInternal(String email);
}
