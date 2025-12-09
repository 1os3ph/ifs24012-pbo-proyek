package org.delcom.app.services;

import org.delcom.app.dto.ProfileForm;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

// --- IMPORTS YANG DIPERLUKAN ---
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // Meskipun tidak dipakai di createUser, tetap di-mock

    @InjectMocks
    private UserService userService;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("getUserById: Harus mengembalikan User saat ID ditemukan")
    void testGetUserById_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        User expectedUser = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getUserById: Harus mengembalikan null saat ID User tidak ditemukan")
    void testGetUserById_WhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User actualUser = userService.getUserById(userId);

        assertNull(actualUser);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getUserByEmail: Harus mengembalikan User saat email ditemukan")
    void testGetUserByEmail_WhenUserExists() {
        String email = "test@example.com";
        User expectedUser = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserByEmail(email);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("getUserByEmail: Harus mengembalikan null saat email tidak ditemukan")
    void testGetUserByEmail_WhenUserNotFound() {
        String email = "ghost@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User result = userService.getUserByEmail(email);

        assertNull(result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("createUser: Harus berhasil membuat user baru dengan password mentah")
    void testCreateUser() {
        String rawPassword = "password123";
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser("Budi", "budi@example.com", rawPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("Budi", userCaptor.getValue().getName());
        assertEquals("budi@example.com", userCaptor.getValue().getEmail());
        assertEquals(rawPassword, userCaptor.getValue().getPassword());
    }

    @Test
    @DisplayName("updateUserProfile: Harus mengembalikan null jika User tidak ditemukan")
    void testUpdateUserProfile_WhenUserNotFound() throws IOException {
        UUID userId = UUID.randomUUID();
        ProfileForm dto = new ProfileForm();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        User result = userService.updateUserProfile(userId, dto);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserProfile: Harus berhasil mengupdate NAMA saja tanpa mengubah foto")
    void testUpdateUserProfile_UpdateNameOnly() throws IOException {
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setName("Nama Lama");
        existingUser.setProfilePicture("foto_lama.jpg");

        ProfileForm dto = new ProfileForm();
        dto.setName("Nama Baru");
        dto.setProfilePicture(new MockMultipartFile("file", new byte[0])); // File kosong

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.updateUserProfile(userId, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("Nama Baru", captor.getValue().getName());
        assertEquals("foto_lama.jpg", captor.getValue().getProfilePicture());
    }

    @Test
    @DisplayName("updateUserProfile: Harus berhasil mengupdate NAMA dan FOTO PROFIL")
    void testUpdateUserProfile_UpdateNameAndPicture() throws IOException {
        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        existingUser.setName("Nama Lama");

        ProfileForm dto = new ProfileForm();
        dto.setName("Nama Baru");
        MockMultipartFile newPicture = new MockMultipartFile("picture", "avatar.png", "image/png", "test-data".getBytes());
        dto.setProfilePicture(newPicture);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.updateUserProfile(userId, dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("Nama Baru", savedUser.getName());
        assertNotNull(savedUser.getProfilePicture());
        assertTrue(savedUser.getProfilePicture().endsWith("_avatar.png"));
        assertTrue(Files.exists(tempDir.resolve(savedUser.getProfilePicture())));
    }

    @Test
    @DisplayName("updateUserProfile: Harus membuat folder upload jika belum ada")
    void testUpdateUserProfile_CreatesUploadDirectory() throws IOException {
        Path nonExistentDir = tempDir.resolve("user_uploads");
        ReflectionTestUtils.setField(userService, "uploadDir", nonExistentDir.toString());
        assertFalse(Files.exists(nonExistentDir));

        UUID userId = UUID.randomUUID();
        User existingUser = new User();
        ProfileForm dto = new ProfileForm();
        MockMultipartFile newPicture = new MockMultipartFile("picture", "avatar.png", "image/png", "test-data".getBytes());
        dto.setProfilePicture(newPicture);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.updateUserProfile(userId, dto);

        assertTrue(Files.exists(nonExistentDir));
    }
}