package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Matikan CSRF (Agar form login tidak error 403 Forbidden)
            .csrf(csrf -> csrf.disable())
            
            // 2. Atur Izin Akses
            .authorizeHttpRequests(auth -> auth
                // PENTING: Izinkan SEMUA yang berawalan /auth/ (Login, Register, POST Login)
                // Biarkan Controller AuthView yang menangani logikanya.
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/uploads/**", "/error").permitAll()
                
                // Halaman lain wajib login
                .anyRequest().authenticated()
            )
            
            // 3. Konfigurasi Form Login
            .formLogin(form -> form
                .loginPage("/auth/login") // Kalau user belum login, lempar ke sini
                // HAPUS loginProcessingUrl agar Spring Security TIDAK mencegat POST login
                .permitAll()
            )
            
            // 4. Logout
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}