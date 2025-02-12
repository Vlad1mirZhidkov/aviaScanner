package com.example.aviaScanner.service;

import org.springframework.stereotype.Service;
import com.example.aviaScanner.repository.AviaScanerUserRepository;
import com.example.aviaScanner.model.AviaScanerUserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Service

public class AviaScanerUserSevice {
    private final AviaScanerUserRepository aviaScanerUserRepository;

    public List<AviaScanerUserEntity> getAllUsers(){
        return aviaScanerUserRepository.findAll();
    }

    public AviaScanerUserEntity getUserById(Long id){
        return aviaScanerUserRepository.findById(id).orElse(null);
    }

    public AviaScanerUserEntity createUser(AviaScanerUserEntity user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        return aviaScanerUserRepository.save(user);
    }

    public void deleteUser(Long id){
        aviaScanerUserRepository.deleteById(id);
    }

    public AviaScanerUserEntity updateUser(Long id, Map<String, Object> updates) {
        AviaScanerUserEntity user = aviaScanerUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("location")) {
            user.setLocation((String) updates.get("location"));
        }
        return aviaScanerUserRepository.save(user);
    }
    
}
