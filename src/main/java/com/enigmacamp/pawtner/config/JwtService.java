package com.enigmacamp.pawtner.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.enigmacamp.pawtner.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {

    @Value("${app.pawtner.jwt.jwt-secret}")
    private String jwtSecret;
    @Value("${app.pawtner.jwt.app-name}")
    private String appName;
    @Value("${app.pawtner.jwt.jwt-expiration}")
    private long jwtExpiration;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return JWT.create()
                    .withIssuer(appName)
                    .withSubject(user.getEmail())
                    .withExpiresAt(Instant.now().plusSeconds(jwtExpiration))
                    .withIssuedAt(Instant.now())
                    .withClaim("userId", user.getId().toString())
                    .withClaim("role", user.getRole().name())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verifyJwtToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getIssuer().equals(appName);
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailByToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
