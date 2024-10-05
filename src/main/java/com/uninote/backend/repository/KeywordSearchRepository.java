package com.uninote.backend.repository;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.KeywordSearch;

@Repository
public interface KeywordSearchRepository extends JpaRepository<KeywordSearch, Long> {
}
