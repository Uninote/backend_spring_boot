package com.uninote.backend.repository;

import com.uninote.backend.entity.TestResultDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestResultDetailRepository extends JpaRepository<TestResultDetail, Long> {
}
