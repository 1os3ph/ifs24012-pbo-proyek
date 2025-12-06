package org.delcom.app.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProfileForm {
    private String name;
    private MultipartFile profilePicture;
}