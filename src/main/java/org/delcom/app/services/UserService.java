package org.delcom.app.services;

import org.delcom.app.dto.UserProfileDTO;
import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir}") // Ambil path folder upload
    private String uploadDir;

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.save(user);
    }

    // === FITUR BARU: UPDATE PROFIL ===
    public User updateUserProfile(UUID userId, UserProfileDTO dto) throws IOException {
        User user = getUserById(userId);
        if (user == null) return null;

        // 1. Update Nama
        user.setName(dto.getName());

        // 2. Update Foto (Jika ada file yang diupload)
        MultipartFile file = dto.getProfilePicture();
        if (file != null && !file.isEmpty()) {
            // Bersihkan nama file
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // Buat nama unik (biar tidak bentrok)
            String uniqueFileName = "user_" + userId + "_" + UUID.randomUUID() + "_" + fileName;

            // Simpan file ke folder
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (var inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Simpan nama file ke DB
                user.setProfilePicture(uniqueFileName);
            }
        }

        return userRepository.save(user);
    }
}