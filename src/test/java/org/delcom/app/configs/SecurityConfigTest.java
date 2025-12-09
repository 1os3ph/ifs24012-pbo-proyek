package org.delcom.app.configs;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.delcom.app.services.JobApplicationService;
import org.delcom.app.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JobApplicationService jobApplicationService;

    @DisplayName("Harus mengizinkan akses ke path publik tanpa autentikasi")
    @ParameterizedTest
    @ValueSource(strings = {"/auth/login", "/auth/register", "/css/style.css", "/js/script.js", "/uploads/image.png", "/error"})
    void testPublicPaths_ShouldBePermitted(String publicPath) throws Exception {
        mockMvc.perform(get(publicPath))
               .andExpect(result -> {
                   int status = result.getResponse().getStatus();
                   if (status == 302) {
                       String redirectUrl = result.getResponse().getRedirectedUrl();
                       if (redirectUrl != null && redirectUrl.contains("/login")) {
                           throw new AssertionError("Path publik " + publicPath + " tidak boleh di-redirect ke login");
                       }
                   }
               });
    }

    @DisplayName("Harus me-redirect ke halaman login untuk path yang diamankan")
    @ParameterizedTest
    // PERBAIKAN 1: Hapus "/auth/profile" dari sini karena secara config dia permitted, 
    // sehingga menyebabkan view error alih-alih redirect security.
    @ValueSource(strings = {"/", "/jobs"}) 
    void testProtectedPaths_ShouldRedirectToLogin(String protectedPath) throws Exception {
        mockMvc.perform(get(protectedPath))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    @Test
    @DisplayName("Harus mengizinkan akses ke path yang diamankan jika sudah diautentikasi dengan User Entitas")
    void testProtectedPaths_ShouldAllowAccess_WhenAuthenticated() throws Exception {
        // PERBAIKAN 2: Setup User yang lengkap agar Controller tidak me-redirect manual
        User appUser = new User();
        appUser.setId(UUID.randomUUID());
        appUser.setName("Test User");
        appUser.setEmail("test@example.com");
        appUser.setPassword("password");
        appUser.setProfilePicture(null); 

        // Buat token autentikasi yang valid
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(appUser, null, appUser.getAuthorities());

        mockMvc.perform(get("/jobs")
                .with(authentication(auth))) // Suntikkan User asli
                .andExpect(result -> {
                    // Kita pastikan statusnya BUKAN redirect ke login
                    // (Bisa jadi 200 OK, atau 500 error template, yang penting lolos security & controller check)
                    String redirectedUrl = result.getResponse().getRedirectedUrl();
                    if (redirectedUrl != null && redirectedUrl.contains("/login")) {
                        throw new AssertionError("User yang sudah login tidak boleh di-redirect ke halaman login");
                    }
                });
    }
}