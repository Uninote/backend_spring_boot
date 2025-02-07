package com.uninote.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.UserNoteHelped;

import java.util.List;

@Repository
public interface UserNoteHelpedRepository extends JpaRepository<UserNoteHelped, Long> {

    List<UserNoteHelped> findByHelpedTrue(); 

    List<UserNoteHelped> findByUserId(Long userId);

    List<UserNoteHelped> findByNoteId(Long noteId);

    UserNoteHelped findByUserIdAndNoteId(Long userId, Long noteId);

}
