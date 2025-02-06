package com.example.aviaScanner.service;

import org.springframework.stereotype.Service;
import com.example.aviaScanner.repository.AviaScanerUserRepository;
import com.example.aviaScanner.model.AviaScanerUserEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


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

    public AviaScanerUserEntity updateUser(Long id, AviaScanerUserEntity user) {
        AviaScanerUserEntity existingUser = aviaScanerUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден"));
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getLocation() != null) {
            existingUser.setLocation(user.getLocation());
        }
        return aviaScanerUserRepository.save(existingUser);
    }
    
}
