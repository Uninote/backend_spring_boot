package com.uninote.backend.repository;

import com.uninote.backend.entity.SpaceResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceResourceRepository extends JpaRepository<SpaceResource, Long> {
    List<SpaceResource> findAllBySpace_Uuid(String uuid);
}
