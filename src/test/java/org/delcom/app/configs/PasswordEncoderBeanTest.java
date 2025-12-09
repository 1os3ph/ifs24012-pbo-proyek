package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

// TIDAK ADA ANOTASI @WebMvcTest ATAU @SpringBootTest DI SINI
public class PasswordEncoderBeanTest {

    @Test
    @DisplayName("Metode passwordEncoder() harus membuat instance BCryptPasswordEncoder")
    void testPasswordEncoderBean() {
        // Arrange
        SecurityConfig config = new SecurityConfig();

        // Act
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        assertTrue(encoder instanceof BCryptPasswordEncoder, "Bean harus berupa instance dari BCryptPasswordEncoder");

        // Verifikasi fungsionalitasnya
        String rawPassword = "my-secret-password";
        String encodedPassword = encoder.encode(rawPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}