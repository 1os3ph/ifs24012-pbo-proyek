package org.delcom.app.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Membuat instance User baru sebelum setiap test dijalankan
        user = new User();
    }

    @Test
    @DisplayName("Getters dan Setters dari Lombok harus berfungsi")
    void testPojoGettersAndSetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        String name = "Budi Doremi";
        String email = "budi@example.com";
        String password = "password123";
        String profilePic = "budi.png";
        LocalDateTime now = LocalDateTime.now();

        // Act
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setProfilePicture(profilePic);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(profilePic, user.getProfilePicture());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    @DisplayName("getUsername() harus mengembalikan nilai dari field email")
    void testGetUsername_ShouldReturnEmail() {
        // Arrange
        String expectedEmail = "test.user@del.ac.id";
        user.setEmail(expectedEmail);

        // Act
        String actualUsername = user.getUsername();

        // Assert
        assertNotNull(actualUsername);
        assertEquals(expectedEmail, actualUsername, "Username harus sama dengan email");
    }

    @Test
    @DisplayName("getAuthorities() harus mengembalikan 'ROLE_USER'")
    void testGetAuthorities_ShouldReturnRoleUser() {
        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size(), "Harus ada satu role");
        
        GrantedAuthority authority = authorities.iterator().next();
        assertEquals("ROLE_USER", authority.getAuthority(), "Role harus 'ROLE_USER'");
    }

    @Test
    @DisplayName("Metode boolean dari UserDetails harus selalu mengembalikan true")
    void testUserDetailsBooleans_ShouldAllReturnTrue() {
        // Assert
        assertTrue(user.isAccountNonExpired(), "isAccountNonExpired harus true");
        assertTrue(user.isAccountNonLocked(), "isAccountNonLocked harus true");
        assertTrue(user.isCredentialsNonExpired(), "isCredentialsNonExpired harus true");
        assertTrue(user.isEnabled(), "isEnabled harus true");
    }
}