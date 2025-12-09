package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        filter = new RequestLoggingFilter();
        ReflectionTestUtils.setField(filter, "port", 8080);
        ReflectionTestUtils.setField(filter, "livereload", false);
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Harus mencetak log dengan warna HIJAU untuk status 200 OK")
    void testDoFilterInternal_Success200() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/users");
        when(response.getStatus()).thenReturn(200);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1"); // Tambahkan mock untuk kelengkapan

        // Act
        filter.doFilterInternal(request, response, filterChain);
        String loggedOutput = outputStreamCaptor.toString();

        // Assert
        assertTrue(loggedOutput.contains("GET"), "Log harus berisi method GET");
        assertTrue(loggedOutput.contains("/api/users"), "Log harus berisi URI");
        assertTrue(loggedOutput.contains("200"), "Log harus berisi status code 200");
        // PERBAIKAN: Hapus .trim() dari pengecekan startsWith
        assertTrue(loggedOutput.startsWith("\u001B[32m"), "Log harus diawali dengan kode warna HIJAU");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Harus mencetak log dengan warna KUNING untuk status 404 Not Found")
    void testDoFilterInternal_ClientError404() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/non-existent");
        when(response.getStatus()).thenReturn(404);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        filter.doFilterInternal(request, response, filterChain);
        String loggedOutput = outputStreamCaptor.toString();

        // Assert
        assertTrue(loggedOutput.contains("POST"), "Log harus berisi method POST");
        assertTrue(loggedOutput.contains("/non-existent"), "Log harus berisi URI");
        assertTrue(loggedOutput.contains("404"), "Log harus berisi status code 404");
        // PERBAIKAN: Hapus .trim() dari pengecekan startsWith
        assertTrue(loggedOutput.startsWith("\u001B[33m"), "Log harus diawali dengan kode warna KUNING");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Harus mencetak log dengan warna MERAH untuk status 500 Server Error")
    void testDoFilterInternal_ServerError500() throws ServletException, IOException {
        // Arrange
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/data");
        when(response.getStatus()).thenReturn(500);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        filter.doFilterInternal(request, response, filterChain);
        String loggedOutput = outputStreamCaptor.toString();

        // Assert
        assertTrue(loggedOutput.contains("GET"), "Log harus berisi method GET");
        assertTrue(loggedOutput.contains("/api/data"), "Log harus berisi URI");
        assertTrue(loggedOutput.contains("500"), "Log harus berisi status code 500");
        // PERBAIKAN: Hapus .trim() dari pengecekan startsWith
        assertTrue(loggedOutput.startsWith("\u001B[31m"), "Log harus diawali dengan kode warna MERAH");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Tidak boleh mencetak log untuk URI yang diawali '/.well-known'")
    void testDoFilterInternal_ShouldNotLogWellKnownUris() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/.well-known/acme-challenge");

        // Act
        filter.doFilterInternal(request, response, filterChain);
        // Di sini kita tetap pakai trim() karena kita mau memastikan outputnya benar-benar kosong
        String loggedOutput = outputStreamCaptor.toString().trim();

        // Assert
        assertTrue(loggedOutput.isEmpty(), "Seharusnya tidak ada log yang tercetak");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}