package com.uninote.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uninote.backend.entity.Space;
import com.uninote.backend.entity.SpaceChat;

@Repository
public interface SpaceChatRepository extends JpaRepository<SpaceChat, Long> {
    List<SpaceChat> findBySpace(Space space);  

    SpaceChat findFirstBySpace(Space space);

}
