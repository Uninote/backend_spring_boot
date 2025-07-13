package com.uninote.backend.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uninote.backend.config.ContentAccessPolicy;
import com.uninote.backend.config.security.FirebaseAuthentication;
import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.dto.NoteSearchResponse;
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
import com.uninote.backend.service.NoteViewService;
import com.uninote.backend.service.UserFilterSearchService;
import com.uninote.backend.service.UserService;
import com.uninote.backend.service.UserSessionService;
import com.uninote.backend.utils.EncryptionUtil;
import com.uninote.backend.validation.NoteValidation.CreateGroup;

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

    @Autowired
    private ContentAccessPolicy contentAccessPolicy;

    @Autowired
    private NoteViewService noteViewService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private UserFilterSearchService userFilterSearchService;

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    @PostMapping("/encode/{noteId}")
    public ResponseEntity<String> generateToken(@PathVariable Long noteId) {
        try {
            String token = EncryptionUtil.encrypt(noteId);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating token");
        }
    }

    @GetMapping("/decode/{token}")
    public ResponseEntity<Long> viewNoteByToken(@PathVariable String token) {
        try {
            Long noteId = EncryptionUtil.decrypt(token);

            return ResponseEntity.ok(noteId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

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
    public ResponseEntity<?> getNoteById(@PathVariable Long id,
            @RequestParam(defaultValue = "EN") String language,
            HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAnonymous = (auth == null || !auth.isAuthenticated()
                    || "anonymousUser".equals(auth.getPrincipal()));

            if (isAnonymous) {
                HttpSession session = request.getSession(true);
                logger.error("Session ID: " + session.getId());
                logger.error("anonymousViewCount: " + session.getAttribute("anonymousViewCount"));
                LocalDate today = LocalDate.now();
                LocalDate lastViewDate = (LocalDate) session.getAttribute("anonymousLastViewDate");

                if (lastViewDate == null || !lastViewDate.equals(today)) {
                    session.setAttribute("anonymousViewCount", 0);
                    session.setAttribute("anonymousLastViewDate", today);
                }

                Integer anonymousViews = (Integer) session.getAttribute("anonymousViewCount");
                anonymousViews = (anonymousViews == null) ? 0 : anonymousViews;

                if (!contentAccessPolicy.isAccessAllowedForAnonymous(anonymousViews)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Access denied: daily view limit reached");

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(error);
                }

                session.setAttribute("anonymousViewCount", anonymousViews + 1);
            } else {
                logger.error("here auth");
                FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) auth;
                String userUid = firebaseAuth.getUid();

                User user = userRepository.findByFirebaseUid(userUid)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user UID: " + userUid));

                Long uploadedNotes = noteService.countNotesByUserId(user.getId());
                int viewCountToday = noteViewService.getTodayViewCount(user.getId());
                logger.error(uploadedNotes.toString());
                logger.error(String.valueOf(viewCountToday));

                if (!contentAccessPolicy.isAccessAllowedForUser(user)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Access denied: daily view limit reached");

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(error);
                }
                noteViewService.trackView(id, user.getId());
            }

            NoteDTO note = noteService.getNoteById(id, language);
            return ResponseEntity.ok(note);

        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException in getNoteById: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error in getNoteById for note ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "An unexpected error occurred while retrieving the note."));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUser(@PathVariable Long userId,
            @RequestParam(defaultValue = "EN") String language) {
        try {

            List<NoteDTO> notes = noteService.getNotesByUser(userId, language);
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
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndCourse(@PathVariable Long userId,
            @PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
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
    public ResponseEntity<?> saveNote(@Validated(CreateGroup.class) @RequestBody NoteDTO notedto) {
        try {
            Note savedNote = noteService.saveNote(notedto);
            NoteDTO noteDto = EntityToDTOConverter.convertNoteToDTO(savedNote);
            return ResponseEntity.ok(noteDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred while saving the note."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id) {
        noteService.softDeleteNoteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department-semester/{departmentId}/{semester}")
    public ResponseEntity<List<NoteDTO>> getNotesByDepartmentAndSemester(@PathVariable Long departmentId,
            @PathVariable int semester) {
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
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndUniversity(@PathVariable Long userId,
            @PathVariable Long universityId) {
        try {
            User user = userService.getUserById(userId);
            University university = universityRepository.findById(universityId)
                    .orElseThrow(() -> new IllegalArgumentException("University Note Found"));
            List<NoteDTO> notes = noteService.getNotesByUserAndUniversity(user, university);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/user-department/{userId}/{departmentId}")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndDepartment(@PathVariable Long userId,
            @PathVariable Long departmentId) {
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
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndDepartmentAndSemester(@PathVariable Long userId,
            @PathVariable Long departmentId, @PathVariable int semester) {
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
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {
        return noteService.getPublicNotes(page, language, size, sortBy, sortDir);
    }

    @GetMapping("/public/university/{universityId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByUniversity(
            @PathVariable Long universityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));
        Page<NoteDTO> notes = noteService.getPublicNotesByUniversity(university, language, page, size, sortBy, sortDir);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/department/{departmentId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        Department department = new Department();
        department.setId(departmentId);
        Page<NoteDTO> notes = noteService.getPublicNotesByDepartment(department, language, page, size, sortBy, sortDir);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/department-semester/{departmentId}/{semester}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartmentAndSemester(
            @PathVariable Long departmentId,
            @PathVariable int semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        Page<NoteDTO> notes = noteService.getPublicNotesByDepartmentAndSemester(departmentId, semester, language, page,
                size, sortBy, sortDir);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/course/{courseId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Co urse not found"));
        Page<NoteDTO> notes = noteService.getPublicNotesByCourse(course, language, page, size, sortBy, sortDir);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("public/search")
    public NoteSearchResponse searchNotes(@RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {
        return noteService.searchNotesWithEditDistancePaginated(keyword, 0.3, page, size, sortBy, sortDir, language);
    }

    @GetMapping("/search/user/{userId}")
    public Page<NoteDTO> searchUserNotes(@PathVariable Long userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return noteService.searchUserNotes(keyword, userId, page, size, sortBy, sortDir);
    }

    @GetMapping("/public/user-course/{userId}/{courseId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndCourse(@PathVariable Long userId,
            @PathVariable Long courseId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndCourse(user, course);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-department/{userId}/{departmentId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndDepartment(@PathVariable Long userId,
            @PathVariable Long departmentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Department department = new Department();
        department.setId(departmentId);
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndDepartment(user, department);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-department-semester/{userId}/{departmentId}/{semester}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndDepartmentAndSemester(@PathVariable Long userId,
            @PathVariable Long departmentId, @PathVariable int semester) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndDepartmentAndSemester(user, departmentId, semester);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public/user-university/{userId}/{universityId}")
    public ResponseEntity<List<NoteDTO>> getPublicNotesByUserAndUniversity(@PathVariable Long userId,
            @PathVariable Long universityId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));
        List<NoteDTO> notes = noteService.getPublicNotesByUserAndUniversity(user, university);
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody NoteDTO noteDto) {
        Note updatedNote = noteService.updateNote(id, noteDto);
        return ResponseEntity.ok(updatedNote);
    }

    /*
     * @GetMapping("/recommendations/{userId}")
     * public ResponseEntity<List<NoteDTO>> getRecommendations(@PathVariable Long
     * userId) {
     * List<NoteDTO> recommendations = noteService.recommendNotes(userId);
     * return ResponseEntity.ok(recommendations);
     * }
     */

    @GetMapping("/public/saved/{userId}")
    public ResponseEntity<List<NoteDTO>> getPublicSavedNotes(@PathVariable Long userId,
            @RequestParam(defaultValue = "EN") String language) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<NoteDTO> notes = noteService.getPublicSavedNotesByUser(userId, language);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/top/{userId}")
    public ResponseEntity<List<NoteProjection>> getTop3PublicNotesByUser(@PathVariable Long userId,
            @RequestParam int limit, @RequestParam(defaultValue = "EN") String language) {
        List<NoteProjection> topNotes = noteService.getTopPublicNotesByUser(userId, language, limit);

        if (topNotes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(topNotes);
    }

    @GetMapping("/uuid/{uuid}/id")
    public ResponseEntity<Long> getNoteIdByUuid(@PathVariable String uuid) {
        Optional<Long> noteId = noteService.findNoteIdByUuid(uuid);
        return noteId.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{noteId}/uuid")
    public ResponseEntity<String> getUuidByNoteId(@PathVariable Long noteId) {
        Optional<String> uuid = noteService.findUuidByNoteId(noteId);
        return uuid.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/public/recent/department/{departmentId}")
    public ResponseEntity<Page<NoteDTO>> getPublicRecentNotesByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        Department department = new Department();
        department.setId(departmentId);
        Page<NoteDTO> notes = noteService.getRecentPublicNotesByDepartment(department, language, page, size, sortBy,
                sortDir);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public-by-type")
    public Page<NoteDTO> getPublicNotesByType(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "typeId") Long typeId,
            @RequestParam(defaultValue = "EN") String language) {
        return noteService.getPublicNotesByType(language, page, size, sortBy, sortDir, typeId);
    }

    @GetMapping("/public-by-type/university/{universityId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByUniversityByType(
            @PathVariable Long universityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "typeId") Long typeId,
            @RequestParam(defaultValue = "EN") String language) {

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));
        Page<NoteDTO> notes = noteService.getPublicNotesByUniversityByType(university, language, page, size, sortBy,
                sortDir, typeId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public-by-type/department/{departmentId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartmentByType(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "typeId") Long typeId,
            @RequestParam(defaultValue = "EN") String language) {

        Department department = new Department();
        department.setId(departmentId);
        Page<NoteDTO> notes = noteService.getPublicNotesByDepartmentByType(department, language, page, size, sortBy,
                sortDir, typeId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public-by-type/department-semester/{departmentId}/{semester}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByDepartmentAndSemesterByType(
            @PathVariable Long departmentId,
            @PathVariable int semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "typeId") Long typeId,
            @RequestParam(defaultValue = "EN") String language) {

        Page<NoteDTO> notes = noteService.getPublicNotesByDepartmentAndSemesterByType(departmentId, semester, language,
                page, size, sortBy, sortDir, typeId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/public-by-type/course/{courseId}")
    public ResponseEntity<Page<NoteDTO>> getPublicNotesByCourseByType(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "typeId") Long typeId,
            @RequestParam(defaultValue = "EN") String language) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Page<NoteDTO> notes = noteService.getPublicNotesByCourseByType(course, language, page, size, sortBy, sortDir,
                typeId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/user/{userId}/course-notes")
    public ResponseEntity<List<CourseNameDTO>> getCoursesWithNotesByUser(@PathVariable Long userId,
            @RequestParam(defaultValue = "EN") String languageCode) {
        List<CourseNameDTO> courses = noteService.getCoursesWithNotesByUser(userId, languageCode);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/pdf-url/{noteId}")
    public ResponseEntity<String> getPdfUrl(@PathVariable Long noteId) {
        return ResponseEntity.ok(noteService.getPdfUrl(noteId));
    }

    @GetMapping("/note-data/{noteId}")
    public NoteDTO getNoteDataById(@PathVariable Long noteId,
            @RequestParam(defaultValue = "EN") String language) {
        return noteService.getNoteData(noteId, language);
    }

    @GetMapping("/exists-by-slug/{slug}")
    public Boolean getNoteDataById(@PathVariable String slug) {
        return noteService.slugExists(slug);
    }

    @GetMapping("/top-interacted/{userId}")
    public List<NoteDTO> getTopInteractedNotes(@PathVariable Long userId) {
        logger.info("here");
        return noteService.getTopInteractedNotes(userId);
    }

    @GetMapping
    public ResponseEntity<?> getNotes(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long universityId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "true") boolean publicOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "likes") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "EN") String language) {

        try {
            Long userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                FirebaseAuthentication firebaseAuth = (FirebaseAuthentication) auth;
                String userUid = firebaseAuth.getUid();
                User user = userRepository.findByFirebaseUid(userUid).orElse(null);
                if (user != null) {
                    userId = user.getId();
                }
            }

            // Only log search if user is authenticated
            if (userId != null) {
                Long sessionId = userSessionService.findLastSessionForUser(userId);
                userFilterSearchService.logSearch(userId, universityId, departmentId, semester, courseId, sessionId);
            }
            
            // Handle type filtering if typeId is provided
            if (typeId != null && publicOnly) {
                // Process by type with various filters
                if (courseId != null) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
                    return ResponseEntity.ok(noteService.getPublicNotesByCourseByType(
                            course, language, page, size, sortBy, sortDir, typeId));
                }

                if (departmentId != null && semester != null) {
                    return ResponseEntity.ok(noteService.getPublicNotesByDepartmentAndSemesterByType(
                            departmentId, semester, language, page, size, sortBy, sortDir, typeId));
                }

                if (departmentId != null) {
                    Department department = new Department();
                    department.setId(departmentId);
                    return ResponseEntity.ok(noteService.getPublicNotesByDepartmentByType(
                            department, language, page, size, sortBy, sortDir, typeId));
                }

                if (universityId != null) {
                    University university = universityRepository.findById(universityId)
                            .orElseThrow(() -> new IllegalArgumentException("University not found"));
                    return ResponseEntity.ok(noteService.getPublicNotesByUniversityByType(
                            university, language, page, size, sortBy, sortDir, typeId));
                }

                // If no specific entity filter but typeId exists
                return ResponseEntity.ok(noteService.getPublicNotesByType(
                        language, page, size, sortBy, sortDir, typeId));
            }

            // Original logic for public notes
            if (publicOnly) {
                if (courseId != null) {
                    Course course = courseRepository.findById(courseId)
                            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
                    return ResponseEntity
                            .ok(noteService.getPublicNotesByCourse(course, language, page, size, sortBy, sortDir));
                }

                if (departmentId != null && semester != null) {
                    return ResponseEntity.ok(noteService.getPublicNotesByDepartmentAndSemester(
                            departmentId, semester, language, page, size, sortBy, sortDir));
                }

                if (departmentId != null) {
                    Department department = new Department();
                    department.setId(departmentId);
                    return ResponseEntity.ok(noteService.getPublicNotesByDepartment(
                            department, language, page, size, sortBy, sortDir));
                }

                if (universityId != null) {
                    University university = universityRepository.findById(universityId)
                            .orElseThrow(() -> new IllegalArgumentException("University not found"));
                    return ResponseEntity.ok(noteService.getPublicNotesByUniversity(
                            university, language, page, size, sortBy, sortDir));
                }

                return ResponseEntity.ok(noteService.getPublicNotes(page, language, size, sortBy, sortDir));
            }
            // Non-public notes
            else {
                if (courseId != null) {
                    Course course = new Course();
                    course.setId(courseId);
                    return ResponseEntity.ok(noteService.getNotesByCourse(course));
                }

                if (departmentId != null && semester != null) {
                    return ResponseEntity.ok(noteService.getNotesByDepartmentAndSemester(departmentId, semester));
                }

                if (departmentId != null) {
                    return ResponseEntity.ok(noteService.getNotesByDepartment(departmentId));
                }

                if (universityId != null) {
                    return ResponseEntity.ok(noteService.getNotesByUniversity(universityId));
                }

                if (semester != null) {
                    return ResponseEntity.ok(noteService.getNotesBySemester(semester));
                }

                return ResponseEntity.badRequest().body("At least one filter parameter must be provided");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}
