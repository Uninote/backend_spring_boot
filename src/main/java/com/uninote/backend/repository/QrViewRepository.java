package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uninote.backend.entity.QrView;

public interface QrViewRepository extends JpaRepository<QrView, Long>{

    @Query(value = "select count(*) from admin.qr_views", nativeQuery = true)
    Long countAll();
    
}
