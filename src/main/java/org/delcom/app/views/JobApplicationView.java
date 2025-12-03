package org.delcom.app.views;

import org.delcom.app.dto.JobApplicationDTO;
import org.delcom.app.entities.JobApplication;
import org.delcom.app.entities.User;
import org.delcom.app.services.JobApplicationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/jobs")
public class JobApplicationView {

    private final JobApplicationService jobService;

    public JobApplicationView(JobApplicationService jobService) {
        this.jobService = jobService;
    }

    private User getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    // LIST DATA (Dashboard)
    @GetMapping
    public String listJobs(Model model) {
        User user = getAuthUser();
        // Kalau belum login, lempar ke halaman login
        if (user == null) return "redirect:/auth/login";

        List<JobApplication> jobs = jobService.getAllJobApplications(user.getId());
        model.addAttribute("jobs", jobs);
        model.addAttribute("user", user);
        
        // Untuk Chart sederhana (Pie Chart Status)
        // Kita hitung manual disini biar mudah ditampilkan di HTML
        long appliedCount = jobs.stream().filter(j -> "Applied".equals(j.getStatus())).count();
        long interviewCount = jobs.stream().filter(j -> "Interview".equals(j.getStatus())).count();
        long rejectedCount = jobs.stream().filter(j -> "Rejected".equals(j.getStatus())).count();
        long offeredCount = jobs.stream().filter(j -> "Offered".equals(j.getStatus())).count();
        
        model.addAttribute("statApplied", appliedCount);
        model.addAttribute("statInterview", interviewCount);
        model.addAttribute("statRejected", rejectedCount);
        model.addAttribute("statOffered", offeredCount);

        return "jobs/list";
    }

    // FORM TAMBAH
    @GetMapping("/create")
    public String createPage(Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("jobForm", new JobApplicationDTO());
        return "jobs/form";
    }

    // PROSES SIMPAN (CREATE)
    @PostMapping("/create")
    public String saveJob(@ModelAttribute JobApplicationDTO dto, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        try {
            jobService.saveJobApplication(dto, user.getId());
            redirectAttributes.addFlashAttribute("success", "Lamaran berhasil disimpan!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload gambar.");
        }
        return "redirect:/jobs";
    }
    
    // FORM EDIT
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable UUID id, Model model) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";
        
        JobApplication job = jobService.getJobApplicationById(id, user.getId());
        if (job == null) return "redirect:/jobs";
        
        // Convert Entity to DTO
        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(job.getId());
        dto.setCompanyName(job.getCompanyName());
        dto.setPosition(job.getPosition());
        dto.setPlatform(job.getPlatform());
        dto.setStatus(job.getStatus());
        dto.setExpectedSalary(job.getExpectedSalary());
        dto.setAppliedDate(job.getAppliedDate());
        dto.setNotes(job.getNotes());
        dto.setExistingLogoPath(job.getCompanyLogo()); // Penting buat preview gambar
        
        model.addAttribute("jobForm", dto);
        return "jobs/form";
    }
    
    // PROSES UPDATE
    @PostMapping("/edit")
    public String updateJob(@ModelAttribute JobApplicationDTO dto, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        try {
            jobService.saveJobApplication(dto, user.getId());
            redirectAttributes.addFlashAttribute("success", "Lamaran berhasil diperbarui!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload gambar.");
        }
        return "redirect:/jobs";
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String deleteJob(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getAuthUser();
        if (user == null) return "redirect:/auth/login";

        jobService.deleteJobApplication(id, user.getId());
        redirectAttributes.addFlashAttribute("success", "Data berhasil dihapus.");
        return "redirect:/jobs";
    }
}