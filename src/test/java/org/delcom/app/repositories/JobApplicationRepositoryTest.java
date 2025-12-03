package org.delcom.app.repositories;

import org.delcom.app.entities.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // Tambahan 1
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// TAMBAHAN PENTING: Perintah untuk pakai DB Asli (PostgreSQL)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobApplicationRepository jobRepository;

    @Test
    void findAllByUserId_ShouldOnlyReturnDataForSpecificUser() {
        UUID userA = UUID.randomUUID();
        
        JobApplication jobA = new JobApplication();
        jobA.setUserId(userA);
        jobA.setCompanyName("Perusahaan A");
        jobA.setPosition("Dev");
        jobA.setStatus("Applied");
        entityManager.persist(jobA);

        entityManager.flush();

        List<JobApplication> results = jobRepository.findAllByUserId(userA);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCompanyName()).isEqualTo("Perusahaan A");
    }
}