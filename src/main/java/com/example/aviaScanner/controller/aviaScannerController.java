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

@RestController
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
    public ResponseEntity<?> createUser(@RequestBody AviaScanerUserEntity user) {
        try {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Email не может быть пустым");
            }
            if (user.getName() == null || user.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Имя не может быть пустым");
            }
            if (user.getPhone() == null || user.getPhone().isEmpty()) {
                return ResponseEntity.badRequest().body("Телефон не может быть пустым");
            }
            if (user.getLocation() == null || user.getLocation().isEmpty()) {
                return ResponseEntity.badRequest().body("Местоположение не может быть пустым");
            }
            
            return ResponseEntity.ok(aviaScanerUserSevice.createUser(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ошибка при создании пользователя: " + e.getMessage());
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
        public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
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


