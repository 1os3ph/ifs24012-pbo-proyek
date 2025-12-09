package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Validasi harus sukses jika semua data valid")
    void testValidRegisterForm() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail("budi@del.ac.id");
        form.setPassword("rahasia");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty(), "Seharusnya tidak ada error validasi");
    }

    @Test
    @DisplayName("Validasi harus gagal jika format email salah")
    void testInvalidEmail() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail("budi-bukan-email"); // Format salah
        form.setPassword("123");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty(), "Seharusnya ada error validasi email");
        
        // Cek pesan error (opsional)
        boolean hasEmailError = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Format email salah"));
        assertTrue(hasEmailError);
    }

    @Test
    @DisplayName("Validasi harus gagal jika field wajib kosong")
    void testEmptyFields() {
        RegisterForm form = new RegisterForm();
        // Semua field null/kosong

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        // Harusnya ada 3 error (Name, Email, Password)
        assertTrue(violations.size() >= 3);
    }
}