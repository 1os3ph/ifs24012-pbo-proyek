package org.delcom.app.services;

import org.delcom.app.dto.JobApplicationDTO;
import org.delcom.app.entities.JobApplication;
import org.delcom.app.repositories.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobRepository;

    @InjectMocks
    private JobApplicationService jobService;

    // JUnit 5 akan membuat folder sementara untuk kita
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        // Suntikkan path folder sementara ke field 'uploadDir' di service
        ReflectionTestUtils.setField(jobService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("Harus mengembalikan semua lamaran milik user")
    void testGetAllJobApplications() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(jobRepository.findAllByUserId(userId)).thenReturn(Collections.singletonList(new JobApplication()));

        // Act
        List<JobApplication> result = jobService.getAllJobApplications(userId);

        // Assert
        assertFalse(result.isEmpty());
        verify(jobRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Harus mengembalikan satu lamaran spesifik milik user")
    void testGetJobApplicationById() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(new JobApplication());

        // Act
        JobApplication result = jobService.getJobApplicationById(jobId, userId);

        // Assert
        assertNotNull(result);
        verify(jobRepository, times(1)).findByIdAndUserId(jobId, userId);
    }

    @Test
    @DisplayName("Harus berhasil menyimpan lamaran BARU tanpa file")
    void testSaveNewJobApplication_NoFile() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setCompanyName("Google");
        dto.setPosition("Software Engineer");

        // Act
        jobService.saveJobApplication(dto, userId);

        // Assert
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobRepository, times(1)).save(captor.capture());

        JobApplication savedJob = captor.getValue();
        assertEquals("Google", savedJob.getCompanyName());
        assertEquals(userId, savedJob.getUserId());
        assertNull(savedJob.getCompanyLogo()); // Pastikan logo null
    }

    @Test
    @DisplayName("Harus berhasil menyimpan lamaran BARU dengan file")
    void testSaveNewJobApplication_WithFile() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setCompanyName("Facebook");
        MockMultipartFile file = new MockMultipartFile("logoFile", "logo.png", "image/png", "test data".getBytes());
        dto.setLogoFile(file);

        // Act
        jobService.saveJobApplication(dto, userId);

        // Assert
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobRepository, times(1)).save(captor.capture());
        
        JobApplication savedJob = captor.getValue();
        assertNotNull(savedJob.getCompanyLogo());
        assertTrue(savedJob.getCompanyLogo().endsWith("_logo.png"));
        assertTrue(Files.exists(tempDir.resolve(savedJob.getCompanyLogo()))); // Cek file benar-benar dibuat
    }

    @Test
    @DisplayName("Harus berhasil MENGUPDATE lamaran yang ada")
    void testUpdateExistingJobApplication() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        JobApplication existingJob = new JobApplication();
        existingJob.setCompanyName("Old Company");

        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(jobId);
        dto.setCompanyName("New Company");

        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(existingJob);

        // Act
        jobService.saveJobApplication(dto, userId);

        // Assert
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobRepository, times(1)).save(captor.capture());
        assertEquals("New Company", captor.getValue().getCompanyName());
    }

    @Test
    @DisplayName("Harus melempar exception saat MENGUPDATE lamaran yang tidak ada")
    void testUpdateJobApplication_NotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(jobId);

        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jobService.saveJobApplication(dto, userId);
        });

        assertEquals("Data not found or unauthorized", exception.getMessage());
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("Harus berhasil menghapus lamaran yang ada")
    void testDeleteJobApplication_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        JobApplication jobToDelete = new JobApplication();

        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(jobToDelete);

        // Act
        jobService.deleteJobApplication(jobId, userId);

        // Assert
        verify(jobRepository, times(1)).delete(jobToDelete);
    }

    @Test
    @DisplayName("Tidak melakukan apa-apa saat menghapus lamaran yang tidak ada")
    void testDeleteJobApplication_NotFound() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        when(jobRepository.findByIdAndUserId(jobId, userId)).thenReturn(null);

        // Act
        jobService.deleteJobApplication(jobId, userId);

        // Assert
        verify(jobRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Harus membuat folder upload jika belum ada")
    void testSaveJobApplication_CreatesDirectoryIfNotExists() throws IOException {
        // Arrange
        // 1. Definisikan path ke sebuah sub-folder di dalam tempDir yang kita tahu belum ada.
        Path nonExistentUploadDir = tempDir.resolve("new_uploads");

        // 2. Pastikan folder itu memang belum ada sebagai kondisi awal.
        assertFalse(Files.exists(nonExistentUploadDir));

        // 3. Timpa field 'uploadDir' di service untuk menunjuk ke path baru ini.
        ReflectionTestUtils.setField(jobService, "uploadDir", nonExistentUploadDir.toString());

        // Siapkan DTO dan file seperti biasa untuk memicu logika upload.
        UUID userId = UUID.randomUUID();
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setCompanyName("Startup Inc.");
        MockMultipartFile file = new MockMultipartFile("file", "pitch-deck.pdf", "application/pdf", "data".getBytes());
        dto.setLogoFile(file);

        // Act: Panggil metode yang akan kita uji.
        jobService.saveJobApplication(dto, userId);

        // Assert
        // 4. Verifikasi bahwa folder tersebut SEKARANG SUDAH ADA.
        //    Ini membuktikan bahwa baris Files.createDirectories() telah dieksekusi.
        assertTrue(Files.exists(nonExistentUploadDir), "Folder upload seharusnya sudah dibuat oleh service");
    }

    @Test
    @DisplayName("Tidak boleh memproses upload jika file ada tapi kosong (0 byte)")
    void testSaveJobApplication_WithEmptyFile() throws IOException {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setCompanyName("Empty File Corp");
        
        // Buat file yang objeknya ada (tidak null), tapi isinya kosong (byte array 0)
        MockMultipartFile emptyFile = new MockMultipartFile("logoFile", "empty.png", "image/png", new byte[0]);
        dto.setLogoFile(emptyFile);

        // Act
        jobService.saveJobApplication(dto, userId);

        // Assert
        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobRepository, times(1)).save(captor.capture());

        JobApplication savedJob = captor.getValue();
        // Pastikan nama tersimpan, TAPI logo harus null (karena file kosong dianggap tidak upload)
        assertEquals("Empty File Corp", savedJob.getCompanyName());
        assertNull(savedJob.getCompanyLogo(), "Logo harus null karena file kosong");
    }
}