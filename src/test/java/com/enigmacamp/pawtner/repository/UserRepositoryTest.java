package com.enigmacamp.pawtner.repository;

import com.enigmacamp.pawtner.constant.UserRole;
import com.enigmacamp.pawtner.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByEmail_ShouldReturnUser_WhenEmailExists() {
        // Given
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .passwordHash("password")
                .role(UserRole.CUSTOMER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void testExistsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Given
        User user = User.builder()
                .name("Exists User")
                .email("exists@example.com")
                .passwordHash("password")
                .role(UserRole.CUSTOMER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // When
        boolean exists = userRepository.existsByEmail("exists@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByResetPasswordToken_ShouldReturnUser_WhenTokenExists() {
        // Given
        User user = User.builder()
                .name("Token User")
                .email("tokenuser@example.com")
                .passwordHash("password")
                .role(UserRole.CUSTOMER)
                .resetPasswordToken("reset-token-123")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> foundUser = userRepository.findByResetPasswordToken("reset-token-123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getResetPasswordToken()).isEqualTo("reset-token-123");
    }

    @Test
    void testFindByResetPasswordToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // When
        Optional<User> foundUser = userRepository.findByResetPasswordToken("nonexistent-token");

        // Then
        assertThat(foundUser).isNotPresent();
    }
}
