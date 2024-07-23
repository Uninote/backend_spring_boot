package com.uninote.backend.repository;

import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.UniscoreIncreaseType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniscoreIncreaseTypeRepository extends JpaRepository<UniscoreIncreaseType, Long> {

    UniscoreIncreaseType findByName(String name);
}
