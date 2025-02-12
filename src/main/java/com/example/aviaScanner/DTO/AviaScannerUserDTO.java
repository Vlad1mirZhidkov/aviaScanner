package com.example.aviaScanner.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;

import lombok.Data;

@Data
public class AviaScannerUserDTO {
    @NotEmpty
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+7|8)\\d{10}$", message = "incorrect phone number")
    private String phone;

    @NotBlank
    private String location;

    @NotNull
    @Past
    private LocalDate birthDate;
} 