package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // <-- IMPORT INI
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PERBAIKAN: Tambahkan excludeAutoConfiguration untuk menonaktifkan security di test ini
@WebMvcTest(controllers = {}, useDefaultFilters = false, 
            excludeAutoConfiguration = SecurityAutoConfiguration.class) 
@Import(WebMvcConfig.class)
public class WebMvcConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @TempDir
    static Path sharedTempDir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> sharedTempDir.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Harus berhasil menyajikan file dari direktori upload yang dikonfigurasi")
    void testUploadsResourceHandlerIsConfigured() throws Exception {
        // Arrange: Buat sebuah file dummy di dalam folder temporer
        String fileName = "test-logo.png";
        Path dummyFile = sharedTempDir.resolve(fileName);
        Files.writeString(dummyFile, "dummy file content");

        // Act & Assert: Sekarang harus mengembalikan 200 OK karena security sudah mati
        mockMvc.perform(get("/uploads/" + fileName))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Harus mengembalikan 404 Not Found untuk file yang tidak ada")
    void testUploadsResourceHandlerReturns404() throws Exception {
        // Act & Assert: Sekarang harus mengembalikan 404 karena security sudah mati
        mockMvc.perform(get("/uploads/non-existent-file.jpg"))
                .andExpect(status().isNotFound());
    }
}