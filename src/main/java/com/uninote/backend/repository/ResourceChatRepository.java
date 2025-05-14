package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.User;

public interface ResourceChatRepository extends JpaRepository<ResourceChat,Long>{
    Optional<ResourceChat> findByChat_Uuid(String uuid);

    @Query("SELECT rc FROM ResourceChat rc JOIN FETCH rc.chat c JOIN FETCH rc.resource r WHERE c.user = :user")
    List<ResourceChat> findAllWithChatAndResourceByUser(@Param("user") User user);

    List<ResourceChat> findAllByChat_User(User user);

}
