package com.example.aviaScanner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.aviaScanner.service.AviaScanerUserSevice;
import com.example.aviaScanner.model.AviaScanerUserEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;  
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.Map;
import com.example.aviaScanner.DTO.AviaScannerUserDTO;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import com.example.aviaScanner.DTO.ErrorResponse;

@RestController
@Validated
@RequestMapping("/api")
public class aviaScannerController {
    private final AviaScanerUserSevice aviaScanerUserSevice;

    public aviaScannerController(AviaScanerUserSevice aviaScanerUserSevice) {
        this.aviaScanerUserSevice = aviaScanerUserSevice;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AviaScanerUserEntity>> getUsers() {
        return ResponseEntity.ok(aviaScanerUserSevice.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<AviaScannerUserDTO> createUser(@Valid @RequestBody AviaScannerUserDTO userDTO) {
        try {
            AviaScannerUserDTO createdUser = aviaScanerUserSevice.createUser(userDTO);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id){
        try{
            if(aviaScanerUserSevice.getUserById(id) == null){
                throw new Exception("User not found");
            }
            return ResponseEntity.ok(aviaScanerUserSevice.getUserById(id));
        } catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
            errorResponse.setError("Not Found");
            errorResponse.setMessage("User not found");
            errorResponse.setPath("/api/users/" + id);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        try{
            if(aviaScanerUserSevice.getUserById(id) == null){
                throw new Exception("User not found");
            }
            aviaScanerUserSevice.deleteUser(id);
            return ResponseEntity.ok("User is deleted");
        } catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
            errorResponse.setError("Not Found");
            errorResponse.setMessage("User not found");
            errorResponse.setPath("/api/users/" + id);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates) {
        try {
            if(aviaScanerUserSevice.getUserById(id) == null){
                throw new Exception("User not found");
            }
            return ResponseEntity.ok(aviaScanerUserSevice.updateUser(id, updates));
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
            errorResponse.setError("Not Found");
            errorResponse.setMessage("User not found");
            errorResponse.setPath("/api/users/" + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
    }
}