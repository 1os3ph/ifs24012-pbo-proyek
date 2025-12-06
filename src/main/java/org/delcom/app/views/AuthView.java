package org.delcom.app.views;

import java.io.IOException;
import java.util.List;

import org.delcom.app.dto.LoginForm;
import org.delcom.app.dto.UserProfileDTO;
import org.delcom.app.dto.RegisterForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/auth")
public class AuthView {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // Inject Bean dari SecurityConfig

    // Hapus AuthTokenService karena tidak dipakai
    public AuthView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String showLogin(Model model, HttpSession session) {
        // Cek apakah sudah login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        
        if (isLoggedIn) {
            return "redirect:/jobs"; // Langsung ke Dashboard
        }

        model.addAttribute("loginForm", new LoginForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
    }

    @PostMapping("/login/post")
    public String postLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        User existingUser = userService.getUserByEmail(loginForm.getEmail());
        if (existingUser == null) {
            bindingResult.rejectValue("email", "error.loginForm", "Pengguna ini belum terdaftar");
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        // Pakai passwordEncoder yang di-inject
        boolean isPasswordMatch = passwordEncoder.matches(loginForm.getPassword(), existingUser.getPassword());
        
        if (!isPasswordMatch) {
            // Error ditaruh di field password atau email (bebas), atau global error
            bindingResult.rejectValue("password", "error.loginForm", "Email atau kata sandi salah");
            return ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN;
        }

        // === PROSES LOGIN ===
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                existingUser,
                null,
                authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Simpan ke Session agar diingat browser
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);

        return "redirect:/jobs"; // Redirect ke Dashboard
    }

    @GetMapping("/register")
    public String showRegister(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        
        if (isLoggedIn) {
            return "redirect:/jobs";
        }

        model.addAttribute("registerForm", new RegisterForm());
        return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
    }

    @PostMapping("/register/post")
    public String postRegister(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        User existingUser = userService.getUserByEmail(registerForm.getEmail());
        if (existingUser != null) {
            bindingResult.rejectValue("email", "error.registerForm", "Email sudah terdaftar");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        // Pakai passwordEncoder yang di-inject
        String hashPassword = passwordEncoder.encode(registerForm.getPassword());

        User createdUser = userService.createUser(
                registerForm.getName(),
                registerForm.getEmail(),
                hashPassword);

        if (createdUser == null) {
            bindingResult.rejectValue("email", "error.registerForm", "Gagal membuat pengguna baru");
            return ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER;
        }

        redirectAttributes.addFlashAttribute("success", "Akun berhasil dibuat! Silakan login.");
        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Hapus session
        return "redirect:/auth/login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // Ambil user yang sedang login
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/login";
        }

        // Ambil data user dari database berdasarkan email yang login
        // (Kita query lagi biar datanya paling update)
        User user = (User) auth.getPrincipal(); // Atau userService.getUserByEmail(auth.getName())
        
        model.addAttribute("user", user);
        return "auth/profile"; // Nama file HTML yang akan kita buat
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/login";
        }

        // Ambil data terbaru dari DB
        User currentUser = (User) auth.getPrincipal();
        User dbUser = userService.getUserById(currentUser.getId());

        // Masukkan data lama ke Form DTO
        UserProfileDTO dto = new UserProfileDTO();
        dto.setName(dbUser.getName());
        
        model.addAttribute("profileForm", dto);
        return "auth/edit-profile"; // Mengarah ke edit-profile.html
    }

    // PROSES SIMPAN PROFIL (POST)
    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("profileForm") UserProfileDTO profileForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        
        if (bindingResult.hasErrors()) {
            return "auth/edit-profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        try {
            // 1. Update Data di Database
            User updatedUser = userService.updateUserProfile(currentUser.getId(), profileForm);

            // 2. UPDATE SESSION (PENTING!)
            // Agar nama/foto di Navbar langsung berubah tanpa logout
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUser, 
                    auth.getCredentials(), 
                    auth.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal upload foto: " + e.getMessage());
        }

        return "redirect:/auth/profile";
    }
}