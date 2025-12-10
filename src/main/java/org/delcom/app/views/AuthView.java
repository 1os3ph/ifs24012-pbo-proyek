package org.delcom.app.views;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.ProfileForm; // Pastikan ini ProfileForm
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class AuthView {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // WAJIB ADA: Agar layout main.html bisa menampilkan nama & foto di sidebar
    @ModelAttribute("user")
    public User addCurrentUserToModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        if (addCurrentUserToModel() != null) return "redirect:/jobs";
        model.addAttribute("loginForm", new LoginForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    @PostMapping("/login/post")
    public String postLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult, HttpSession session) {

        if (bindingResult.hasErrors()) return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;

        User user = userService.getUserByEmail(loginForm.getEmail());
        if (user == null || !passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            bindingResult.rejectValue("email", "error.loginForm", "Email atau password salah");
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        // Manual Login Session
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return "redirect:/jobs";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (addCurrentUserToModel() != null) return "redirect:/jobs";
        model.addAttribute("registerForm", new RegisterForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    @PostMapping("/register/post")
    public String postRegister(@Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult, RedirectAttributes redirect) {

        if (bindingResult.hasErrors()) return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.registerForm", "Email sudah terdaftar");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        userService.createUser(form.getName(), form.getEmail(), passwordEncoder.encode(form.getPassword()));
        redirect.addFlashAttribute("success", "Akun berhasil dibuat! Silakan login.");
        return "redirect:/auth/login";
    }

    // --- PROFIL ---
    @GetMapping("/profile")
    public String showProfile() {
        return "auth/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        User user = addCurrentUserToModel();
        if (user == null) return "redirect:/auth/login";

        // Pakai ProfileForm (BUKAN UserProfileDTO)
        ProfileForm dto = new ProfileForm();
        dto.setName(user.getName());
        dto.setUniversity(user.getUniversity());
        dto.setLastEducation(user.getLastEducation());
        
        model.addAttribute("profileForm", dto);
        return "auth/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("profileForm") ProfileForm profileForm,
                                RedirectAttributes redirect, HttpSession session) {
        
        User currentUser = addCurrentUserToModel();
        if (currentUser == null) return "redirect:/auth/login";

        try {
            // Update via Service
            User updatedUser = userService.updateUserProfile(currentUser.getId(), profileForm);

            // Update Session agar foto berubah realtime
            Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUser, null, updatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            redirect.addFlashAttribute("success", "Profil berhasil diperbarui!");
        } catch (IOException e) {
            redirect.addFlashAttribute("error", "Gagal upload foto.");
        }
        return "redirect:/auth/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}