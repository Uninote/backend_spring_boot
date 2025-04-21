package com.uninote.backend.repository;

import com.uninote.backend.entity.MessageMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageMediaRepository extends JpaRepository<MessageMedia, Long> {
    List<MessageMedia> findByMessageId(Long messageId);
}
