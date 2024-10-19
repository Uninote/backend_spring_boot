package com.uninote.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.UserFilterSearch;

public interface UserFilterSearchRepository extends JpaRepository<UserFilterSearch, Long> {

}
