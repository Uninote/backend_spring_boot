package com.uninote.backend.repository;

import com.uninote.backend.entity.Comment;
import com.uninote.backend.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNote(Note note);
}
