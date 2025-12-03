package org.delcom.app.views;

import org.delcom.app.entities.JobApplication;
import org.delcom.app.entities.User;
import org.delcom.app.services.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobApplicationView.class)
class JobApplicationViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobApplicationService jobService;

    private User mockUser;

    @BeforeEach
    void setupUser() {
        // Simulasi User Login (Penting karena Controller mengecek SecurityContext)
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@test.com");
        mockUser.setPassword("pass");
        
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(mockUser, null, List.of())
        );
    }

    @Test
    void listJobs_ShouldCalculateChartStatsCorrectly() throws Exception {
        // Arrange: Siapkan data dummy untuk chart
        JobApplication j1 = new JobApplication(); j1.setStatus("Applied");
        JobApplication j2 = new JobApplication(); j2.setStatus("Applied");
        JobApplication j3 = new JobApplication(); j3.setStatus("Offered");
        
        List<JobApplication> jobs = Arrays.asList(j1, j2, j3);
        
        when(jobService.getAllJobApplications(mockUser.getId())).thenReturn(jobs);

        // Act & Assert
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(view().name("jobs/list"))
                // CEK RUBRIK POINT 8 (Chart Data):
                // Harus ada 2 Applied dan 1 Offered di Model
                .andExpect(model().attribute("statApplied", 2L))
                .andExpect(model().attribute("statOffered", 1L))
                .andExpect(model().attribute("statRejected", 0L))
                .andExpect(model().attribute("jobs", hasSize(3)));
    }

    @Test
    void saveJob_ShouldRedirectAfterSuccess() throws Exception {
        mockMvc.perform(post("/jobs/create")
                .with(csrf())
                .param("companyName", "Tokopedia")
                .param("position", "Dev")
                .param("status", "Interview"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jobs"));
    }
}