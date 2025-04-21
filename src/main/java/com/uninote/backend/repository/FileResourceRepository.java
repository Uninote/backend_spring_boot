package com.uninote.backend.repository;


import com.uninote.backend.entity.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileResourceRepository extends JpaRepository<FileResource, Long> {
}
