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
import com.example.aviaScanner.DTO.AviaScanerUserDTO;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

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
    public ResponseEntity<AviaScanerUserDTO> createUser(@Valid @RequestBody AviaScanerUserDTO userDTO) {
        try {
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AviaScanerUserEntity> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(aviaScanerUserSevice.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        aviaScanerUserSevice.deleteUser(id);
        return ResponseEntity.ok("User is deleted");
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @Valid @RequestBody Map<String, Object> updates) {
        try {
            AviaScanerUserEntity existingUser = aviaScanerUserSevice.getUserById(id);
            if (updates.containsKey("email")) {
                existingUser.setEmail((String) updates.get("email"));
            }
            if (updates.containsKey("name")) {
                existingUser.setName((String) updates.get("name"));
            }
            if (updates.containsKey("phone")) {
                existingUser.setPhone((String) updates.get("phone"));
            }
            if (updates.containsKey("location")) {
                existingUser.setLocation((String) updates.get("location"));
            }
            return ResponseEntity.ok(aviaScanerUserSevice.updateUser(id, existingUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при частичном обновлении пользователя: " + e.getMessage());
        }
    }
}