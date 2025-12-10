package org.delcom.app.services;

import org.delcom.app.dto.JobApplicationDTO;
import org.delcom.app.entities.JobApplication;
import org.delcom.app.repositories.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository jobRepository;

    // 1. Mengambil nilai "app.upload.dir" dari application.properties
    //    Walaupun di properties kuning/warning, ini tetap akan jalan!
    @Value("${app.upload.dir}")
    private String uploadDir;

    // --- FITUR UTAMA ---

    // Menampilkan semua data milik User yang sedang login
    public List<JobApplication> getAllJobApplications(UUID userId) {
        return jobRepository.findAllByUserId(userId);
    }

    // Menampilkan 1 data detail (Cek userId agar aman)
    public JobApplication getJobApplicationById(UUID id, UUID userId) {
        return jobRepository.findByIdAndUserId(id, userId);
    }

    // Menyimpan Data Baru / Update Data
    public void saveJobApplication(JobApplicationDTO dto, UUID userId) throws IOException {
        JobApplication jobApp;

        // Cek apakah ini Update (ada ID) atau Insert Baru (tidak ada ID)
        if (dto.getId() != null) {
            // Logika Update: Ambil data lama dulu
            jobApp = jobRepository.findByIdAndUserId(dto.getId(), userId);
            if (jobApp == null) {
                throw new RuntimeException("Data not found or unauthorized");
            }
        } else {
            // Logika Insert Baru
            jobApp = new JobApplication();
            jobApp.setUserId(userId); // Set pemilik data
        }

        // Mapping dari DTO ke Entity
        jobApp.setCompanyName(dto.getCompanyName());
        jobApp.setPosition(dto.getPosition());
        jobApp.setWorkMode(dto.getWorkMode());
        jobApp.setLocation(dto.getLocation());
        jobApp.setPlatform(dto.getPlatform());
        jobApp.setStatus(dto.getStatus());
        jobApp.setExpectedSalary(dto.getExpectedSalary());
        jobApp.setAppliedDate(dto.getAppliedDate());
        jobApp.setNotes(dto.getNotes());

        // LOGIKA UPLOAD GAMBAR (LOGO)
        MultipartFile file = dto.getLogoFile();
        if (file != null && !file.isEmpty()) {
            // 1. Bersihkan nama file
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            
            // 2. Buat nama unik agar tidak bentrok (misal: uuid_google.png)
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            // 3. Siapkan folder upload
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // Buat folder jika belum ada
            }

            // 4. Simpan file ke folder
            try (var inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // 5. Simpan nama file ke Database
                jobApp.setCompanyLogo(uniqueFileName);
            }
        }

        // Simpan ke database
        jobRepository.save(jobApp);
    }

    // Menghapus Data
    public void deleteJobApplication(UUID id, UUID userId) {
        JobApplication jobApp = jobRepository.findByIdAndUserId(id, userId);
        if (jobApp != null) {
            // Optional: Hapus file gambar fisiknya juga jika mau hemat storage
            jobRepository.delete(jobApp);
        }
    }
}