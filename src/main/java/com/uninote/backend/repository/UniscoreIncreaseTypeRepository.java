package com.uninote.backend.repository;

import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.UniscoreIncreaseType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniscoreIncreaseTypeRepository extends JpaRepository<UniscoreIncreaseType, Long> {

    Optional<UniscoreIncreaseType> findById(Long id);
}
