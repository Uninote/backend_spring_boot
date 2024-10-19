package com.uninote.backend.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.DepartmentRequest;

@Repository
public interface DepartmentRequestRepository extends JpaRepository<DepartmentRequest, Long> {
}
