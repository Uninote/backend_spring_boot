package com.uninote.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.service.UserNoteHelpedService;

@RestController
@RequestMapping("/note-interactions")
public class UserNoteHelpedController {
    
    @Autowired
    private UserNoteHelpedService userNoteHelpedService;

    @PostMapping
    public void saveInteraction(@RequestParam Long userId, 
        @RequestParam Long noteId, 
        @RequestParam Boolean helped) {

        userNoteHelpedService.saveInteraction(userId, noteId, helped);
    }   



    


}


