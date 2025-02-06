package com.example.aviaScanner.repository;

import java.util.Optional;
import java.util.List;
import com.example.aviaScanner.model.AviaScanerUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AviaScanerUserRepository extends JpaRepository<AviaScanerUserEntity, Long> {
    Optional<AviaScanerUserEntity> findById(Long id);
    List<AviaScanerUserEntity> findAllByName(String name);
}

