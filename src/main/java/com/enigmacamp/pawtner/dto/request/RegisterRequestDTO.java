package com.enigmacamp.pawtner.dto.request;

import com.enigmacamp.pawtner.constant.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDTO {
    private String email;
    private String phoneNumber;
    private String password;
    private String name;
    private String address;
    private UserRole role;
}
