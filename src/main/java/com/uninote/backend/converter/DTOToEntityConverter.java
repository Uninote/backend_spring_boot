package com.uninote.backend.converter;

import org.springframework.beans.factory.annotation.Autowired;

import com.uninote.backend.dto.BadgeDTO;
import com.uninote.backend.entity.Badge;
import com.uninote.backend.entity.BadgeType;
import com.uninote.backend.repository.BadgeTypeRepository;

public class DTOToEntityConverter {

    

    public static Badge convertDTOToBadge(BadgeDTO dto, BadgeTypeRepository badgeTypeRepository) { 
        BadgeType type = badgeTypeRepository.findByName(dto.getTypeName()).orElseThrow(() -> new IllegalArgumentException("Invalid name"));
        Badge badge = new Badge();
        badge.setId(dto.getId());
        badge.setName(dto.getName());
        badge.setDescription(dto.getDescription());
        badge.setImageUrl(dto.getImageUrl());
        badge.setRequirement(dto.getRequirement());
        badge.setType(type);
        return badge;
    }
}
