package com.uninote.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.CreatedTest;

public interface CreatedTestRepository extends JpaRepository<CreatedTest, Long> {
}
