package com.example.aviaScanner.DTO;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}