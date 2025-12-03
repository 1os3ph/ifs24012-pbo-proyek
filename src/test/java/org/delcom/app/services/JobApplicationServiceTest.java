package org.delcom.app.services;

import org.delcom.app.dto.JobApplicationDTO;
import org.delcom.app.entities.JobApplication;
import org.delcom.app.repositories.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobRepository;

    @InjectMocks
    private JobApplicationService jobService;

    // Membuat folder temporary untuk simulasi upload file biar folder asli gak kotor
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Menyuntikkan path temporary ke variabel 'uploadDir' di Service
        ReflectionTestUtils.setField(jobService, "uploadDir", tempDir.toString());
    }

    @Test
    void saveJobApplication_WithImage_ShouldSaveAndUpload() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("logoFile", "test.png", "image/png", "test-data".getBytes());
        
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setCompanyName("Google");
        dto.setPosition("Dev");
        dto.setStatus("Applied");
        dto.setLogoFile(file);

        // Act
        jobService.saveJobApplication(dto, userId);

        // Assert
        // 1. Pastikan repository dipanggil untuk save
        verify(jobRepository, times(1)).save(any(JobApplication.class));
        
        // 2. Pastikan file fisik benar-benar tercipta di folder temp
        assertTrue(Files.list(tempDir).count() > 0, "File gambar harusnya ter-upload");
    }

    @Test
    void deleteJobApplication_ShouldCallDelete_WhenFound() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        JobApplication job = new JobApplication();
        
        when(jobRepository.findByIdAndUserId(id, userId)).thenReturn(job);

        jobService.deleteJobApplication(id, userId);

        verify(jobRepository, times(1)).delete(job);
    }
}