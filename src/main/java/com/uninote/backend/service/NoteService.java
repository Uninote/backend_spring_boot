package com.uninote.backend.service;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    
    
    @Cacheable("notes")
    public NoteDTO getNoteById(Long id) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Note not found"));
        return convertToDTO(note);
    }

    public List<NoteDTO> getNotesByUser(User user) {
        return noteRepository.findByUser(user).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByCourse(Course course) {
        return noteRepository.findByCourse(course).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByUserAndCourse(User user, Course course) {
        return noteRepository.findByUserAndCourse(user, course).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByTitle(String title) {
        return noteRepository.findByTitle(title).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByDepartment(Long departmentId) {
        return noteRepository.findByCourse_Department_Id(departmentId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByUniversity(Long universityId) {
        return noteRepository.findByCourse_Department_University_Id(universityId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesBySemester(int semester) {
        return noteRepository.findByCourse_Semester(semester).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByUserAndUniversity(User user, University university) {
        return noteRepository.findByUserAndUniversity(user, university).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByUserAndDepartment(User user, Department department) {
        return noteRepository.findByUserAndDepartment(user, department).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByUserAndDepartmentAndSemester(User user, Department department, int semester) {
        return noteRepository.findByUserAndDepartmentAndSemester(user, department, semester).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getNotesByDepartmentAndSemester(Long departmentId, int semester) {
        return noteRepository.findByDepartmentAndSemester(departmentId, semester).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNoteById(Long id) {
        noteRepository.deleteById(id);
    }
    public List<NoteDTO> getPublicNotes() {
        return noteRepository.findPublicNotes().stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    public List<NoteDTO> getPublicNotesByUserAndUniversity(User user, University university) {
        return noteRepository.findPublicNotesByUserAndUniversity(user, university).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getPublicNotesByUserAndCourse(User user, Course course) {
        return noteRepository.findPublicNotesByUserAndCourse(user, course).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    public List<NoteDTO> getPublicNotesByUserAndDepartment(User user, Department department) {
        return noteRepository.findPublicNotesByUserAndDepartment(user, department).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getPublicNotesByUserAndDepartmentAndSemester(User user, Long departmentId, int semester) {
        return noteRepository.findPublicNotesByUserAndDepartmentAndSemester(user, departmentId, semester).stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    public List<NoteDTO> getPublicNotesByDepartmentAndSemester(Long departmentId, int semester) {
        return noteRepository.findPublicNotesByDepartmentAndSemester(departmentId, semester).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    public List<NoteDTO> getPublicNotesByDepartment(Department department) {
        return noteRepository.findPublicNotesByDepartment(department).stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    public List<NoteDTO> getPublicNotesByCourse(Course course) {
        return noteRepository.findPublicNotesByCourse(course).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<NoteDTO> getPublicNotesByUniversity(University university) {
        return noteRepository.findPublicNotesByUniversity(university).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private NoteDTO convertToDTO(Note note) {
        return new NoteDTO(
                note.getId(),
                note.getTitle(),
                note.getDescription(),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.getPdfUrl(),
                note.getCourse().getDepartment().getId(),
                note.getCourse().getDepartment().getUniversity().getId(),
                note.getViews(),
                note.getLikes(),
                note.getIsPublic()
        );
    }
}
