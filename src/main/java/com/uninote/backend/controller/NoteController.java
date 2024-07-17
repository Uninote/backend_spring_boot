package com.uninote.backend.controller;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.NoteService;
import com.uninote.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        try {
            Note note = noteService.getNoteById(id);
            return ResponseEntity.ok(note);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            List<NoteDTO> notes = noteService.getNotesByUser(user);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }



    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Note>> getNotesByCourse(@PathVariable Long courseId) {
        Course course = new Course();
        course.setId(courseId);
        List<Note> notes = noteService.getNotesByCourse(course);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<List<Note>> getNotesByTitle(@PathVariable String title) {
        List<Note> notes = noteService.getNotesByTitle(title);
        return ResponseEntity.ok(notes);
    }


    

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Note>> getNotesByDepartment(@PathVariable Long departmentId) {
        List<Note> notes = noteService.getNotesByDepartment(departmentId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<Note>> getNotesBySemester(@PathVariable int semester) {
        List<Note> notes = noteService.getNotesBySemester(semester);
        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<Note> saveNote(@RequestBody Note note) {
        Note savedNote = noteService.saveNote(note);
        return ResponseEntity.ok(savedNote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id) {
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }
}
