package com.example.aviaScanner.DTO;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AviaScannerUserDTO {
    private Long id;
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
    @JsonFormat(pattern = "dd.MM.yyyy")
    @Past(message = "birth date must be in the past")
    private LocalDate birthDate;
} 