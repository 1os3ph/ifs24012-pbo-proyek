package org.delcom.app.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        // TRIK UNTUK COVERAGE:
        // Kita arahkan ke sub-folder "images" yang BELUM ADA saat Spring start.
        // Karena folder belum ada, path.toUri() TIDAK akan diakhiri "/",
        // sehingga logika 'if (!resourcePath.endsWith("/"))' di WebMvcConfig akan dieksekusi (HIJAU).
        registry.add("app.upload.dir", () -> sharedTempDir.resolve("images").toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Harus berhasil menyajikan file dari direktori upload yang dikonfigurasi")
    void testUploadsResourceHandlerIsConfigured() throws Exception {
        // Arrange: 
        // 1. Definisikan path folder yang sama dengan di @DynamicPropertySource
        Path uploadDir = sharedTempDir.resolve("images");
        
        // 2. Buat direktorinya sekarang (karena tadi belum ada)
        Files.createDirectories(uploadDir);

        // 3. Buat file dummy di dalamnya
        String fileName = "test-logo.png";
        String fileContent = "dummy content";
        Path dummyFile = uploadDir.resolve(fileName);
        Files.writeString(dummyFile, fileContent);

        // Act & Assert
        // Request ke /uploads/ harus berhasil mengambil file dari folder /images/ tadi
        mockMvc.perform(get("/uploads/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().string(fileContent));
    }

    @Test
    @DisplayName("Harus mengembalikan 404 Not Found untuk file yang tidak ada")
    void testUploadsResourceHandlerReturns404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/uploads/non-existent-file.jpg"))
                .andExpect(status().isNotFound());
    }
}