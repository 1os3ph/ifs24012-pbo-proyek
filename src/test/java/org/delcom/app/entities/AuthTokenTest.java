package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenTest {

    @Test
    @DisplayName("Constructor dengan parameter harus mengisi field dan createdAt")
    void testConstructorWithParameters() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String tokenStr = "abc-123";

        // Act
        AuthToken token = new AuthToken(userId, tokenStr);

        // Assert
        assertNotNull(token.getCreatedAt(), "CreatedAt tidak boleh null saat inisialisasi");
        assertEquals(userId, token.getUserId());
        assertEquals(tokenStr, token.getToken());
    }

    @Test
    @DisplayName("Default constructor dan semua setter/getter harus berfungsi")
    void testDefaultConstructorAndSetters() {
        // Arrange
        AuthToken token = new AuthToken(); // Menggunakan default constructor
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String tokenStr = "xyz-789";

        // Act
        token.setId(id);
        token.setUserId(userId);
        token.setToken(tokenStr);

        // Assert: Memastikan getter mengembalikan nilai yang sudah di-set
        // Test ini mencakup setId(), setUserId(), setToken(), dan getId()
        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(tokenStr, token.getToken());
    }

    @Test
    @DisplayName("Metode onCreate (@PrePersist) harus mengatur nilai createdAt")
    void testOnCreateMethod() {
        // Arrange
        AuthToken token = new AuthToken();
        // Pastikan createdAt awalnya null setelah default constructor
        assertNull(token.getCreatedAt(), "CreatedAt seharusnya null pada awalnya");

        // Act
        // Kita panggil langsung metode 'onCreate()' untuk menguji logikanya
        token.onCreate();

        // Assert
        // Pastikan createdAt sekarang sudah terisi setelah 'onCreate()' dipanggil
        assertNotNull(token.getCreatedAt(), "CreatedAt seharusnya terisi setelah onCreate() dipanggil");
        
        // Opsional: Cek apakah waktunya mendekati waktu sekarang
        assertTrue(token.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}