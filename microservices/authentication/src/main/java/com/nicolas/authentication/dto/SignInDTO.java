package com.nicolas.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInDTO {
    @NotNull(message = "Name can't be null")
    private String name;

    @NotNull(message = "Email can't be null")
    @Email
    private String email;

    @NotNull
    private String password;
    @NotNull
    private String confirmPassword;
}
