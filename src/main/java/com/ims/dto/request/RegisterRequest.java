package com.ims.dto.request;

import com.ims.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 80)
    @Pattern(regexp = "^[A-Za-z0-9_.-]+$",
            message = "Username may contain only letters, digits, '.', '_' and '-'")
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
