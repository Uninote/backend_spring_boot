package com.uninote.backend.service;

import com.uninote.backend.converter.DTOToEntityConverter;
import com.uninote.backend.dto.InviteDTO;
import com.uninote.backend.entity.Invite;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.InviteRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.converter.EntityToDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InviteService {

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BadgeService badgeService;

    public InviteDTO saveInvite(InviteDTO inviteDTO) {
        User user = userRepository.findById(inviteDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Invite invite = new Invite(user, null, null);
        invite = inviteRepository.save(invite);
        return EntityToDTOConverter.convertInviteToDTO(invite);
    }

    public List<InviteDTO> getInvitesByUserId(Long userId) {
        List<Invite> invites = inviteRepository.findByUserId(userId);
        return invites.stream().map(EntityToDTOConverter::convertInviteToDTO).collect(Collectors.toList());
    }

    public InviteDTO acceptInvite(String inviteUuId, Long inviteeId) {
        Invite invite = inviteRepository.findByUuid(inviteUuId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new IllegalArgumentException("Invitee not found"));


        invite.setInvitee(invitee);
        invite.setDateOfSignUp(LocalDateTime.now());
        userService.updateUniScore(invite.getUser(), 21L);
        invite = inviteRepository.save(invite);
        badgeService.checkBadgesForUser(invite.getUser().getId());
        return EntityToDTOConverter.convertInviteToDTO(invite);
    }

    public String generateInviteLink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        Invite invite = new Invite(user, null, null);
        invite = inviteRepository.save(invite);
        return "https://uninote.gr/invite?inviteId=" + invite.getUuid();
    }
}
