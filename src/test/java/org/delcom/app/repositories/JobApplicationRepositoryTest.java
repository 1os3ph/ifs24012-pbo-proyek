package org.delcom.app.repositories;

import org.delcom.app.entities.JobApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // <-- IMPORT INI
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace; // <-- IMPORT INI

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // <-- TAMBAHKAN ANOTASI INI
public class JobApplicationRepositoryTest {

    @Autowired
    private JobApplicationRepository jobRepository;

    @Test
    @DisplayName("Harus hanya menampilkan lamaran milik user tertentu (Multi-tenancy)")
    void testFindAllByUserId() {
        // ... (Isi method test tetap sama)
        // Arrange
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        JobApplication job1 = new JobApplication();
        job1.setUserId(user1);
        job1.setCompanyName("Google");
        job1.setPosition("Backend Dev");
        job1.setStatus("Applied");
        job1.setAppliedDate(LocalDate.now());

        JobApplication job2 = new JobApplication();
        job2.setUserId(user2); // User berbeda
        job2.setCompanyName("Facebook");
        job2.setPosition("Frontend Dev");
        job2.setStatus("Applied");
        job2.setAppliedDate(LocalDate.now());

        jobRepository.save(job1);
        jobRepository.save(job2);

        // Act
        List<JobApplication> user1Jobs = jobRepository.findAllByUserId(user1);

        // Assert
        assertThat(user1Jobs).hasSize(1);
        assertThat(user1Jobs.get(0).getCompanyName()).isEqualTo("Google");
    }
}