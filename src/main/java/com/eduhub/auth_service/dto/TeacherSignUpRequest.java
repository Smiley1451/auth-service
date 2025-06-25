package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeacherSignUpRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    public SignupRequest toSignupRequest() {
        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(Role.TEACHER);
        return request;
    }
}