package org.delcom.app.repositories;

import org.delcom.app.entities.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    // Cari semua data milik user tertentu
    List<JobApplication> findAllByUserId(UUID userId);

    // Cari satu data spesifik milik user tertentu (Security Check)
    JobApplication findByIdAndUserId(UUID id, UUID userId);
    
    // Optional: Hitung status untuk Chart (Pie Chart)
    @Query("SELECT j.status, COUNT(j) FROM JobApplication j WHERE j.userId = :userId GROUP BY j.status")
    List<Object[]> countStatusByUserId(UUID userId);
}