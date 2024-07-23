package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.TestType;

public interface TestTypeRepository extends JpaRepository<TestType,Long> {
}
