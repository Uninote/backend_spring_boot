package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.SaveHistory;

@Repository
public interface SaveHistoryRepository extends JpaRepository<SaveHistory, Long> {

    
}

