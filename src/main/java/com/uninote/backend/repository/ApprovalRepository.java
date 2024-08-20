package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Approval;
import com.uninote.backend.entity.ApprovalId;
import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, ApprovalId> {
    
    @Query("SELECT a FROM Approval a WHERE a.userId = :userId")
    List<Approval> findAllByUserId(@Param("userId") Long userId);
}
