package com.uninote.backend.controller;

import com.uninote.backend.dto.InviteDTO;
import com.uninote.backend.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invites")
public class InviteController {

    @Autowired
    private InviteService inviteService;

    @PostMapping
    public InviteDTO createInvite(@RequestBody InviteDTO inviteDTO) {
        return inviteService.saveInvite(inviteDTO);
    }

    @GetMapping("/user/{userId}")
    public List<InviteDTO> getInvitesByUserId(@PathVariable Long userId) {
        return inviteService.getInvitesByUserId(userId);
    }

    

    @GetMapping("/generate/{userId}")
    public String generateInviteLink(@PathVariable Long userId) {
        return inviteService.generateInviteLink(userId);
    }

    @PostMapping("/accept")
    public InviteDTO acceptInvite(@RequestParam String inviteUuId, @RequestParam Long inviteeId) {
        return inviteService.acceptInvite(inviteUuId, inviteeId);
    }
}
