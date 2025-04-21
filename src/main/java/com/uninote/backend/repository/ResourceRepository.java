package com.uninote.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.Role;

public interface ResourceRepository extends JpaRepository<Resource,Long>{
}
