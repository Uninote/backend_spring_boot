package com.uninote.backend.controller;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.interfaceProjection.NoteProjection;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.NoteService;
import com.uninote.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UniversityRepository universityRepository;


    @GetMapping("/{noteId}/likes/count")
    public ResponseEntity<Long> getTotalLikes(@PathVariable Long noteId) {
        long totalLikes = noteService.getTotalLikes(noteId);
        return ResponseEntity.ok(totalLikes);
    }

    @GetMapping("/{noteId}/likes/user/{userId}")
    public ResponseEntity<Boolean> hasUserLiked(@PathVariable Long noteId, @PathVariable Long userId) {
        boolean hasLiked = noteService.hasUserLiked(noteId, userId);
        return ResponseEntity.ok(hasLiked);
    }
    @GetMapping("/{noteId}/saves/user/{userId}")
    public ResponseEntity<Boolean> hasUserSaved(@PathVariable Long noteId, @PathVariable Long userId) {
        boolean hasSaved = noteService.hasUserSaved(noteId, userId);
        return ResponseEntity.ok(hasSaved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteDTO> getNoteById(@PathVariable Long id) {
        try {
            NoteDTO note = noteService.getNoteById(id);
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
            
            List<NoteDTO> notes = noteService.getNotesByUser(userId);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }



    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<NoteDTO>> getNotesByCourse(@PathVariable Long courseId) {
        Course course = new Course();
        course.setId(courseId);
        List<NoteDTO> notes = noteService.getNotesByCourse(course);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/user-course/{userId}/{courseId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User Not found"));
        List<NoteDTO> notes = noteService.getNotesByUserAndCourse(user, course);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<List<NoteDTO>> getNotesByTitle(@PathVariable String title) {
        List<NoteDTO> notes = noteService.getNotesByTitle(title);
        return ResponseEntity.ok(notes);
    }


    

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<NoteDTO>> getNotesByDepartment(@PathVariable Long departmentId) {
        List<NoteDTO> notes = noteService.getNotesByDepartment(departmentId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/university/{universityId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUniversity(@PathVariable Long universityId) {
        List<NoteDTO> notes = noteService.getNotesByUniversity(universityId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<NoteDTO>> getNotesBySemester(@PathVariable int semester) {
        List<NoteDTO> notes = noteService.getNotesBySemester(semester);
        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<NoteDTO> saveNote(@RequestBody NoteDTO notedto) {
        Note savedNote = noteService.saveNote(notedto);
        NoteDTO noteDto= EntityToDTOConverter.convertNoteToDTO(savedNote);
        return ResponseEntity.ok(noteDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id) {
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department-semester/{departmentId}/{semester}")
    public ResponseEntity<List<NoteDTO>> getNotesByDepartmentAndSemester(@PathVariable Long departmentId, @PathVariable int semester) {
        try {
            List<NoteDTO> notes = noteService.getNotesByDepartmentAndSemester(departmentId, semester);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }



     @GetMapping("/user-university/{userId}/{universityId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndUniversity(@PathVariable Long userId, @PathVariable Long universityId) {
        try {
            User user = userService.getUserById(userId);
            University university = universityRepository.findById(universityId).orElseThrow(() -> new IllegalArgumentException("University Note Found"));
            List<NoteDTO> notes = noteService.getNotesByUserAndUniversity(user, university);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/user-department/{userId}/{departmentId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndDepartment(@PathVariable Long userId, @PathVariable Long departmentId) {
        try {
            User user = userService.getUserById(userId);
            Department department = new Department();
            department.setId(departmentId);
            List<NoteDTO> notes = noteService.getNotesByUserAndDepartment(user, department);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/user-department-semester/{userId}/{departmentId}/{semester}")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndDepartmentAndSemester(@PathVariable Long userId, @PathVariable Long departmentId, @PathVariable int semester) {
        try {
            User user = userService.getUserById(userId);
            Department department = new Department();
            department.setId(departmentId);
            List<NoteDTO> notes = noteService.getNotesByUserAndDepartmentAndSemester(user, department, semester);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/public")
public Page<NoteDTO> getPublicNotes(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
) {
    return noteService.getPublicNotes(page, size, sortBy, sortDir);
}

@GetMapping("/public/university/{universityId}")
public ResponseEntity<Page<NoteDTO>> getPublicNotesByUniversity(
        @PathVariable Long universityId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

    University university = universityRepository.findById(universityId)
            .orElseThrow(() -> new IllegalArgumentException("University not found"));
    Page<NoteDTO> notes = noteService.getPublicNotesByUniversity(university, page, size, sortBy, sortDir);
    return ResponseEntity.ok(notes);
}

@GetMapping("/public/department/{departmentId}")
public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartment(
        @PathVariable Long departmentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

    Department department = new Department();
    department.setId(departmentId);
    Page<NoteDTO> notes = noteService.getPublicNotesByDepartment(department, page, size, sortBy, sortDir);
    return ResponseEntity.ok(notes);
}

@GetMapping("/public/department-semester/{departmentId}/{semester}")
public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartmentAndSemester(
        @PathVariable Long departmentId,
        @PathVariable int semester,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

    Page<NoteDTO> notes = noteService.getPublicNotesByDepartmentAndSemester(departmentId, semester, page, size, sortBy, sortDir);
    return ResponseEntity.ok(notes);
}

@GetMapping("/public/course/{courseId}")
public ResponseEntity<Page<NoteDTO>> getPublicNotesByCourse(
        @PathVariable Long courseId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {

    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    Page<NoteDTO> notes = noteService.getPublicNotesByCourse(course, page, size, sortBy, sortDir);
    return ResponseEntity.ok(notes);
}


    @GetMapping("/search")
    public Page<NoteDTO> searchNotes(@RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {
        return noteService.searchNotes(keyword, page, size, sortBy, sortDir);
    }

    @GetMapping("/search/user/{userId}")
    public Page<NoteDTO> searchUserNotes(@PathVariable Long userId,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "likes") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {
        return noteService.searchUserNotes(keyword,userId, page, size, sortBy, sortDir);
    }
    @GetMapping("/public/user-course/{userId}/{courseId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndCourse(@PathVariable Long userId, @PathVariable Long courseId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndCourse(user, course);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-department/{userId}/{departmentId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndDepartment(@PathVariable Long userId, @PathVariable Long departmentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Department department = new Department();
        department.setId(departmentId);
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndDepartment(user, department);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-department-semester/{userId}/{departmentId}/{semester}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndDepartmentAndSemester(@PathVariable Long userId, @PathVariable Long departmentId, @PathVariable int semester) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndDepartmentAndSemester(user, departmentId, semester);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-university/{userId}/{universityId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndUniversity(@PathVariable Long userId, @PathVariable Long universityId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        University university = universityRepository.findById(universityId).orElseThrow(() -> new IllegalArgumentException("University not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndUniversity(user, university);
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody NoteDTO noteDto) {
        Note updatedNote = noteService.updateNote(id, noteDto);
        return ResponseEntity.ok(updatedNote);
    }

   /*  @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<NoteDTO>> getRecommendations(@PathVariable Long userId) {
        List<NoteDTO> recommendations = noteService.recommendNotes(userId);
        return ResponseEntity.ok(recommendations);
    }*/

    @GetMapping("/public/saved/{userId}")
    public ResponseEntity<List<NoteDTO>> getPublicSavedNotes(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<NoteDTO> notes = noteService.getPublicSavedNotesByUser(userId);
        return ResponseEntity.ok(notes);
    }


    @GetMapping("/top/{userId}")
    public ResponseEntity<List<NoteProjection>> getTop3PublicNotesByUser(@PathVariable Long userId, @RequestParam int limit) {
        List<NoteProjection> topNotes = noteService.getTopPublicNotesByUser(userId, limit);
        
        if (topNotes.isEmpty()) {
            return ResponseEntity.noContent().build();  
        }
        
        return ResponseEntity.ok(topNotes);
    }
    
}
