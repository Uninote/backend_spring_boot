package com.uninote.backend.service;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    
     @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NoteLikeRepository likeRepository;

    @Autowired
    private BadgeService badgeService; 

    @Autowired
    private UserService userService;


     public Note updateNote(Long noteId, NoteDTO noteDto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));

        if (noteDto.getCourseId() != null) {
            Course course = courseRepository.findById(noteDto.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid course ID: " + noteDto.getCourseId()));
            note.setCourse(course);
        }
        if (noteDto.getUserId() != null) {
            User user = userRepository.findById(noteDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + noteDto.getUserId()));
            note.setUser(user);
        }
        if (noteDto.getTitle() != null) {
            note.setTitle(noteDto.getTitle());
        }
        if (noteDto.getDescription() != null) {
            note.setDescription(noteDto.getDescription());
        }
        if (noteDto.getPdfUrl() != null) {
            note.setPdfUrl(noteDto.getPdfUrl());
        }
        if (noteDto.getIsPublic() != null) {
            note.setIsPublic(noteDto.getIsPublic());
        }
        if (noteDto.getFilename() != null) {
            note.setFilename(noteDto.getFilename());
        }

        note.setUpdatedAt(LocalDateTime.now());

        return noteRepository.save(note);
    }

    public long getTotalLikes(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
        return likeRepository.countByNote(note);
    }

    public boolean hasUserLiked(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
        
                Optional<NoteLike> like = likeRepository.findByNoteIdAndUserId(noteId, userId);
                return like.isPresent();
            }
    
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
                note.getCourse().getId(),
                note.getUser().getId(),
                note.getTitle(),
                note.getDescription(),
                note.getPdfUrl(),
                note.getFilename(),
                note.getIsPublic()
        );
    }

    
    public Note saveNote(NoteDTO noteDto) {
        Course course = courseRepository.findById(noteDto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));
        User user = userRepository.findById(noteDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        System.out.println(course.toString()+user.toString());
        Note note = new Note();
        note.setCourse(course);
        note.setUser(user);
        note.setTitle(noteDto.getTitle());
        note.setDescription(noteDto.getDescription());
        note.setPdfUrl(noteDto.getPdfUrl());
        note.setFilename(noteDto.getFilename());
        note.setIsPublic(noteDto.getIsPublic());

        Note savedNote = noteRepository.save(note);
        long noteCount = noteRepository.countByUserId(user.getId());
        if (noteCount == 1) {
            userService.updateUniScore(user, 22L); // Assign a higher UniScore for the first note upload
        } else {
            userService.updateUniScore(user, 23L); // Regular UniScore for subsequent note uploads
        }
        badgeService.checkBadgesForUser(user.getId()); 
        return savedNote;
    }

    
}
