package com.uninote.backend.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninote.backend.entity.Note;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteRepository;


import java.util.List;


@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with id: " + id));
    }

    public List<NoteDTO> getNotesByUser(User user) {
        return noteRepository.findByUser(user);
    }

    public List<Note> getNotesByCourse(Course course) {
        return noteRepository.findByCourse(course);
    }

    public List<Note> getNotesByTitle(String title) {
        return noteRepository.findByTitle(title);
    }

    
    
    public List<Note> getNotesByUserAndCourse(User user, Course course) {
        return noteRepository.findByUserAndCourse(user, course);
    }

    public List<Note> getNotesByDepartment(Long departmentId) {
        return noteRepository.findByDepartment(departmentId);
    }

    public List<Note> getNotesBySemester(int semester) {
        return noteRepository.findBySemester(semester);
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNoteById(Long id) {
        noteRepository.deleteById(id);
    }
}
