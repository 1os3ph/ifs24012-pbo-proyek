package org.delcom.app.services;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthTokenServiceTest {

    // Membuat repository tiruan (mock)
    @Mock
    private AuthTokenRepository authTokenRepository;

    // Menyuntikkan (inject) mock di atas ke dalam service yang akan diuji
    @InjectMocks
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("Harus mengembalikan AuthToken jika ditemukan di repository")
    void testFindUserToken_WhenTokenExists() {
        // Arrange (Persiapan)
        UUID userId = UUID.randomUUID();
        String tokenString = "valid-token-123";
        AuthToken expectedToken = new AuthToken(userId, tokenString);

        // Atur perilaku mock:
        // "Jika authTokenRepository.findUserToken dipanggil dengan userId dan tokenString ini,
        //  maka kembalikan objek expectedToken"
        when(authTokenRepository.findUserToken(userId, tokenString)).thenReturn(expectedToken);

        // Act (Eksekusi)
        // Panggil metode service yang sedang kita uji
        AuthToken actualToken = authTokenService.findUserToken(userId, tokenString);

        // Assert (Verifikasi)
        // 1. Pastikan service mengembalikan objek yang benar
        assertEquals(expectedToken, actualToken, "Token yang dikembalikan harus sama dengan yang dari repository");

        // 2. Pastikan metode di repository dipanggil tepat satu kali dengan parameter yang benar
        verify(authTokenRepository, times(1)).findUserToken(userId, tokenString);
    }

    @Test
    @DisplayName("Harus mengembalikan null jika tidak ditemukan di repository")
    void testFindUserToken_WhenTokenDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String tokenString = "invalid-token-456";

        // Atur perilaku mock untuk mengembalikan null (skenario "tidak ditemukan")
        when(authTokenRepository.findUserToken(userId, tokenString)).thenReturn(null);

        // Act
        AuthToken actualToken = authTokenService.findUserToken(userId, tokenString);

        // Assert
        // 1. Pastikan service mengembalikan null
        assertNull(actualToken, "Seharusnya mengembalikan null jika token tidak ditemukan");

        // 2. Pastikan metode repository tetap dipanggil
        verify(authTokenRepository, times(1)).findUserToken(userId, tokenString);
    }
}