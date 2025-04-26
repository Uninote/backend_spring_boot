package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.User;

public interface ResourceChatRepository extends JpaRepository<ResourceChat,Long>{
    Optional<ResourceChat> findByChat_Uuid(String uuid);

    List<ResourceChat> findAllByChat_User(User user);

}
