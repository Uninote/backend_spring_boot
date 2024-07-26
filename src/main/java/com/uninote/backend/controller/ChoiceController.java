package com.uninote.backend.controller;

import com.uninote.backend.entity.Choice;
import com.uninote.backend.service.ChoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/choices")
public class ChoiceController {

    @Autowired
    private ChoiceService choiceService;

    @GetMapping
    public List<Choice> getAllChoices() {
        return choiceService.getAllChoices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Choice> getChoiceById(@PathVariable Long id) {
        Optional<Choice> choice = choiceService.getChoiceById(id);
        return choice.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Choice createChoice(@RequestBody Choice choice) {
        return choiceService.saveChoice(choice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Choice> updateChoice(@PathVariable Long id, @RequestBody Choice choice) {
        if (!choiceService.getChoiceById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        choice.setId(id);
        Choice updatedChoice = choiceService.saveChoice(choice);
        return ResponseEntity.ok(updatedChoice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChoice(@PathVariable Long id) {
        if (!choiceService.getChoiceById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        choiceService.deleteChoice(id);
        return ResponseEntity.noContent().build();
    }
}
