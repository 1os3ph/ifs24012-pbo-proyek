package org.delcom.app.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StartupInfoLoggerTest {

    @Mock
    private ApplicationReadyEvent mockEvent;
    @Mock
    private ConfigurableApplicationContext mockContext;
    @Mock
    private ConfigurableEnvironment mockEnvironment;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private StartupInfoLogger logger;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        logger = new StartupInfoLogger();
        
        // Gunakan lenient() di sini karena setup ini dipanggil di semua test
        lenient().when(mockEvent.getApplicationContext()).thenReturn(mockContext);
        lenient().when(mockContext.getEnvironment()).thenReturn(mockEnvironment);
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Harus mencetak URL dan status LiveReload ENABLED")
    void testOnApplicationEvent_WithLiveReloadEnabled() {
        // Gunakan lenient() untuk menghindari 'Strict Stubbing Exception' jika urutan panggil beda
        lenient().when(mockEnvironment.getProperty("server.port", "8080")).thenReturn("8888");
        lenient().when(mockEnvironment.getProperty("server.servlet.context-path", "/")).thenReturn("/app");
        lenient().when(mockEnvironment.getProperty("server.address", "localhost")).thenReturn("127.0.0.1");
        lenient().when(mockEnvironment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(true);
        lenient().when(mockEnvironment.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");

        logger.onApplicationEvent(mockEvent);
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("URL: http://127.0.0.1:8888/app"));
        assertTrue(output.contains("LiveReload: ENABLED (port 35729)"));
    }

    @Test
    @DisplayName("Harus mencetak status LiveReload DISABLED saat properti false")
    void testOnApplicationEvent_WithLiveReloadDisabled() {
        lenient().when(mockEnvironment.getProperty("server.port", "8080")).thenReturn("8080");
        lenient().when(mockEnvironment.getProperty("server.servlet.context-path", "/")).thenReturn("/");
        lenient().when(mockEnvironment.getProperty("server.address", "localhost")).thenReturn("localhost");
        lenient().when(mockEnvironment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);
        // Stubbing ini tetap diperlukan meski disabled, karena kode tetap memanggilnya
        lenient().when(mockEnvironment.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");

        logger.onApplicationEvent(mockEvent);
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("URL: http://localhost:8080"));
        assertTrue(output.contains("LiveReload: DISABLED"));
    }
}