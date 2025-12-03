package org.delcom.app.views;

import org.delcom.app.entities.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeView {

    // HAPUS JobApplicationService karena tidak dipakai di sini
    // (Logika data ada di JobApplicationView)

    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Cek Login
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/auth/login";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        // HAPUS 'User authUser = ...' karena tidak dipakai
        // Kita langsung redirect saja
        return "redirect:/jobs";
    }
}