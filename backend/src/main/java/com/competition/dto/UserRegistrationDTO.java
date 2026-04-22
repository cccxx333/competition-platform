package com.competition.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserRegistrationDTO {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username length must be between 3 and 50")
    private String username;

    @Size(max = 64, message = "displayName length must be <= 64")
    private String displayName;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password length must be at least 6")
    private String password;

    private String role;
    private String realName;
    private String school;
    private String major;
    private String grade;
    private String phone;
}
