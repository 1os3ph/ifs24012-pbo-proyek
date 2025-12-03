package org.delcom.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterForm {
    @NotEmpty(message = "Nama wajib diisi")
    private String name;

    @NotEmpty(message = "Email wajib diisi")
    @Email(message = "Format email salah")
    private String email;

    @NotEmpty(message = "Password wajib diisi")
    private String password;
}