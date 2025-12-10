package org.delcom.app.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class JobApplicationDTO {
    
    private UUID id;
    private String companyName;
    private String position;
    private String workMode;
    private String location;
    private String platform;
    private String status;
    private Integer expectedSalary;
    private LocalDate appliedDate;
    private String notes;

    // Khusus menangkap file upload dari Form HTML
    private MultipartFile logoFile;
    
    // Untuk menampilkan gambar lama saat Edit
    private String existingLogoPath;
}