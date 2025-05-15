package com.uninote.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uninote.backend.dto.ResourceChatSummaryDTO;
import com.uninote.backend.entity.Resource;
import com.uninote.backend.entity.ResourceChat;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.User;

public interface ResourceChatRepository extends JpaRepository<ResourceChat,Long>{
    Optional<ResourceChat> findByChat_Uuid(String uuid);

    List<ResourceChat> findAllByChat_User(User user);

    @Query("SELECT new com.uninote.backend.dto.ResourceChatSummaryDTO(" +
            "c.chat.id, " +
            "c.chat.uuid, " +
            "c.resource.title, " +
            "c.chat.createdAt, " +
            "c.chat.title) " +
            "FROM ResourceChat c " +
            "WHERE c.chat.user = :user AND c.chat.uuid IS NOT NULL " +
            "ORDER BY c.chat.updatedAt DESC")
    List<ResourceChatSummaryDTO> findAllSummaryByUser(@Param("user") User user);



}
