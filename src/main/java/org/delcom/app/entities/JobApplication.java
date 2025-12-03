package org.delcom.app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // WAJIB: Untuk memisahkan data antar user (Multi-tenancy)
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String position;

    private String platform; // LinkedIn, JobStreet, dll

    @Column(nullable = false)
    private String status; // Applied, Interview, Rejected

    private Integer expectedSalary;

    private LocalDate appliedDate;

    // Menyimpan nama file gambar logo
    private String companyLogo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}