package com.uninote.backend.service;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.NoteView;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.NoteClickRepository;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.NoteViewRepository;
import com.uninote.backend.repository.UserRepository;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private NoteClickRepository clickRepository;

    @Autowired
    private NoteViewRepository viewRepository;


    @Autowired
    private NoteSaveRepository saveRepository;

    @Autowired
    private NoteViewRepository noteViewRepository;


    @Autowired
    private UserService userService;

    private static final double CLICK_WEIGHT = 0.05;
    private static final double VIEW_WEIGHT = 0.2;
    private static final double LIKE_WEIGHT = 0.3;
    private static final double SAVE_WEIGHT = 0.45;

    private Map<Long, Integer> userIndexMap = new HashMap<>();
    private Map<Long, Integer> noteIndexMap = new HashMap<>();
    private Map<Integer, Long> indexNoteMap = new HashMap<>();


    public double calculateCompositeScore(Long noteId) {
        long clickCount = clickRepository.findByNoteId(noteId).size();
        long viewCount = viewRepository.findByNoteId(noteId).size();
        long likeCount = likeRepository.findByNoteId(noteId).size();
        long saveCount = saveRepository.findByNoteId(noteId).size();

        double compositeScore = (clickCount * CLICK_WEIGHT) +
                                (viewCount * VIEW_WEIGHT) +
                                (likeCount * LIKE_WEIGHT) +
                                (saveCount * SAVE_WEIGHT);

        return compositeScore;
    }

    public Map<Long, Double> calculateAllCompositeScores() {
        List<Note> notes = noteRepository.findAll();
        Map<Long, Double> noteScores = new HashMap<>();
        for (Note note : notes) {
            double compositeScore = calculateCompositeScore(note.getId());
            noteScores.put(note.getId(), compositeScore);
        }
        return noteScores;
    }


    private RealMatrix createRatingsMatrix() {
        List<Long> userIds = userRepository.findAll().stream().map(User::getId).collect(Collectors.toList());
        List<Long> noteIds = noteRepository.findAll().stream().map(Note::getId).collect(Collectors.toList());

        int userCount = userIds.size();
        int noteCount = noteIds.size();

        RealMatrix matrix = MatrixUtils.createRealMatrix(userCount, noteCount);

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            userIndexMap.put(userId, i);
        }

        for (int j = 0; j < noteIds.size(); j++) {
            Long noteId = noteIds.get(j);
            noteIndexMap.put(noteId, j);
            indexNoteMap.put(j, noteId);
        }

        for (NoteClick click : clickRepository.findAll()) {
            int userIndex = userIndexMap.get(click.getUserId());
            int noteIndex = noteIndexMap.get(click.getNoteId());
            matrix.addToEntry(userIndex, noteIndex, CLICK_WEIGHT);
        }

        for (NoteView view : viewRepository.findAll()) {
            int userIndex = userIndexMap.get(view.getUserId());
            int noteIndex = noteIndexMap.get(view.getNoteId());
            matrix.addToEntry(userIndex, noteIndex, VIEW_WEIGHT);
        }

        for (NoteLike like : likeRepository.findAll()) {
            int userIndex = userIndexMap.get(like.getUser().getId());
            int noteIndex = noteIndexMap.get(like.getNote().getId());
            matrix.addToEntry(userIndex, noteIndex, LIKE_WEIGHT);
        }

        for (NoteSave save : saveRepository.findAll()) {
            int userIndex = userIndexMap.get(save.getUserId());
            int noteIndex = noteIndexMap.get(save.getNoteId());
            matrix.addToEntry(userIndex, noteIndex, SAVE_WEIGHT);
        }

        return matrix;
    }

    private int findUserIndex(Long userId) {
        return userIndexMap.getOrDefault(userId, -1);
    }

    private Long findNoteId(int index) {
        return indexNoteMap.get(index);
    }


    public List<NoteDTO> recommendNotes(Long userId) {
        
        List<NoteClick> userClicks = clickRepository.findByUserId(userId);
        List<NoteView> userViews = viewRepository.findByUserId(userId);
        List<NoteLike> userLikes = likeRepository.findByUserId(userId);
        List<NoteSave> userSaves = saveRepository.findByUserId(userId);

        
        if (userClicks.isEmpty() && userViews.isEmpty() && userLikes.isEmpty() && userSaves.isEmpty()) {
            List<Object []> topViewedNotes = noteViewRepository.findTop10ByOrderByViewCountDesc();
            return topViewedNotes.stream()
                    .map(result -> noteRepository.findById((Long) result[0]).orElse(null))
                    .filter(Objects::nonNull)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        Set<Long> userCourseIds = new HashSet<>();
        Set<Long> userDepartmentIds = new HashSet<>();
        Set<Long> userUniversityIds = new HashSet<>();
        userClicks.forEach(click -> {
            Note note = noteRepository.findById(click.getId()).orElse(null);
            if (note != null) {
                userCourseIds.add(note.getCourse().getId());
                userDepartmentIds.add(note.getCourse().getDepartment().getId());
                userUniversityIds.add(note.getCourse().getDepartment().getUniversity().getId());
            }
        });
        userViews.forEach(view -> {
            Note note = noteRepository.findById(view.getId()).orElse(null);
            if (note != null) {
                userCourseIds.add(note.getCourse().getId());
                userDepartmentIds.add(note.getCourse().getDepartment().getId());
                userUniversityIds.add(note.getCourse().getDepartment().getUniversity().getId());
            }
        });
        userLikes.forEach(like -> {
            userCourseIds.add(like.getNote().getCourse().getId());
            userDepartmentIds.add(like.getNote().getCourse().getDepartment().getId());
            userUniversityIds.add(like.getNote().getCourse().getDepartment().getUniversity().getId());
        });
        userSaves.forEach(save -> {
            Note note = noteRepository.findById(save.getNoteId()).orElse(null);
            if (note != null) {
                userCourseIds.add(note.getCourse().getId());
                userDepartmentIds.add(note.getCourse().getDepartment().getId());
                userUniversityIds.add(note.getCourse().getDepartment().getUniversity().getId());
            }
        });

        List<Note> contentBasedRecommendations = noteRepository.findAll().stream()
            .filter(note -> 
                userCourseIds.contains(note.getCourse().getId()) ||
                userDepartmentIds.contains(note.getCourse().getDepartment().getId()) ||
                userUniversityIds.contains(note.getCourse().getDepartment().getUniversity().getId())
            )
            .collect(Collectors.toList());
        
        RealMatrix ratingsMatrix = createRatingsMatrix();
        SingularValueDecomposition svd = new SingularValueDecomposition(ratingsMatrix);
        RealMatrix userFeatures = svd.getU();
        RealMatrix noteFeatures = svd.getV();

        int userIndex = findUserIndex(userId);
        RealMatrix userRatingVector = userFeatures.getRowMatrix(userIndex);

        Map<Long, Double> svdRecommendations = new HashMap<>();
        for (int i = 0; i < noteFeatures.getRowDimension(); i++) {
            double prediction = userRatingVector.multiply(noteFeatures.getRowMatrix(i).transpose()).getEntry(0, 0);
            Long noteId = findNoteId(i);
            svdRecommendations.put(noteId, prediction);
        }

        List<Long> svdRecommendedNoteIds = svdRecommendations.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        
        Set<Long> combinedNoteIds = new LinkedHashSet<>();
        combinedNoteIds.addAll(contentBasedRecommendations.stream().map(Note::getId).collect(Collectors.toList()));
        combinedNoteIds.addAll(svdRecommendedNoteIds);

        List<Note> combinedRecommendations = noteRepository.findAllById(combinedNoteIds);
        return combinedRecommendations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
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
                note.getId(),
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
