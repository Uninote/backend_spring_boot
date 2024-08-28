package com.uninote.backend.repository;

import com.uninote.backend.entity.UniscoreIncreaseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniscoreIncreaseLogRepository extends JpaRepository<UniscoreIncreaseLog, Long> {


    boolean existsByUserIdAndIncreaseTypeId(Long userId, Long increaseTypeId);
}
