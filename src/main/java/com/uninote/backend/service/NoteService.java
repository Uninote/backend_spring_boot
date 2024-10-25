package com.uninote.backend.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.uninote.backend.converter.EntityToDTOConverter;
import com.uninote.backend.dto.CourseNameDTO;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.dto.NoteSearchResponse;
import com.uninote.backend.dto.NoteSearchResult;
import com.uninote.backend.entity.Course;
import com.uninote.backend.entity.CourseName;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.DepartmentName;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.NoteClick;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.NoteType;
import com.uninote.backend.entity.NoteView;
import com.uninote.backend.entity.Season;
import com.uninote.backend.entity.UniscoreIncreaseType;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.UniversityName;
import com.uninote.backend.entity.User;
import com.uninote.backend.entity.UserSeasonPoints;
import com.uninote.backend.interfaceProjection.NoteProjection;
import com.uninote.backend.repository.CourseRepository;
import com.uninote.backend.repository.NoteClickRepository;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.NoteTypeRepository;
import com.uninote.backend.repository.NoteViewRepository;
import com.uninote.backend.repository.SeasonRepository;
import com.uninote.backend.repository.UniscoreIncreaseLogRepository;
import com.uninote.backend.repository.UniscoreIncreaseTypeRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.repository.UserSeasonPointsRepository;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uninote.backend.converter.Converters;
import com.uninote.backend.converter.Converters.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
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

import javax.annotation.PostConstruct;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class NoteService {

    @Autowired
    private UniscoreIncreaseLogRepository uniscoreIncreaseLogsRepository;

    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private NoteSaveRepository noteSaveRepository;

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

    @Autowired
    private NoteTypeRepository noteTypeRepository;

    @Autowired
    private UniscoreIncreaseTypeRepository uniscoreIncreaseTypeRepository;

    
    @Autowired
    private UserSeasonPointsRepository userSeasonPointsRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private SeasonService seasonService;

    private RealMatrix ratingsMatrix;
    private static final double CLICK_WEIGHT = 0.05;
    private static final double VIEW_WEIGHT = 0.2;
    private static final double LIKE_WEIGHT = 0.3;
    private static final double SAVE_WEIGHT = 0.45;

    private Map<Long, Integer> userIndexMap = new HashMap<>();
    private Map<Long, Integer> noteIndexMap = new HashMap<>();
    private Map<Integer, Long> indexNoteMap = new HashMap<>();
    private LocalDateTime lastUpdate;



    /*@PostConstruct
    public void init() {
        recomputeRatingsMatrix();
    }*/

    /*@Scheduled(fixedRate = 3600000) 
    public void recomputeRatingsMatrix() {
        if (ratingsMatrix == null) {
            ratingsMatrix = createRatingsMatrix();
            lastUpdate = LocalDateTime.now();

        } else {
            updateRatingsMatrix();
        }
    }*/

    /*public double calculateCompositeScore(Long noteId) {
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

        RealMatrix matrix = MatrixUtils.createRealMatrix(userCount+5, noteCount+5);

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

    private void updateRatingsMatrix() {
        List<NoteClick> newClicks = clickRepository.findByCreatedAtAfter(lastUpdate);
        List<NoteView> newViews = viewRepository.findByCreatedAtAfter(lastUpdate);
        List<NoteLike> newLikes = likeRepository.findByCreatedAtAfter(lastUpdate);
        List<NoteSave> newSaves = saveRepository.findByCreatedAtAfter(lastUpdate);

        for (NoteClick click : newClicks) {
            int userIndex = userIndexMap.get(click.getUserId());
            int noteIndex = noteIndexMap.get(click.getNoteId());
            ratingsMatrix.addToEntry(userIndex, noteIndex, CLICK_WEIGHT);
        }

        for (NoteView view : newViews) {
            int userIndex = userIndexMap.get(view.getUserId());
            int noteIndex = noteIndexMap.get(view.getNoteId());
            ratingsMatrix.addToEntry(userIndex, noteIndex, VIEW_WEIGHT);
        }

        for (NoteLike like : newLikes) {
            int userIndex = userIndexMap.get(like.getUser().getId());
            int noteIndex = noteIndexMap.get(like.getNote().getId());
            ratingsMatrix.addToEntry(userIndex, noteIndex, LIKE_WEIGHT);
        }

        for (NoteSave save : newSaves) {
            int userIndex = userIndexMap.get(save.getUserId());
            int noteIndex = noteIndexMap.get(save.getNoteId());
            ratingsMatrix.addToEntry(userIndex, noteIndex, SAVE_WEIGHT);
        }
        lastUpdate = LocalDateTime.now();

        
    }

    public List<NoteDTO> recommendNotes(Long userId) {
        List<NoteClick> userClicks = clickRepository.findByUserId(userId);
        List<NoteView> userViews = viewRepository.findByUserId(userId);
        List<NoteLike> userLikes = likeRepository.findByUserId(userId);
        List<NoteSave> userSaves = saveRepository.findByUserId(userId);

        if (userClicks.isEmpty() && userViews.isEmpty() && userLikes.isEmpty() && userSaves.isEmpty()) {
            List<Object[]> topViewedNotes = noteViewRepository.findTop10ByOrderByViewCountDesc();
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
            Note note = noteRepository.findById(click.getNoteId()).orElse(null);
            if (note != null) {
                userCourseIds.add(note.getCourse().getId());
                userDepartmentIds.add(note.getCourse().getDepartment().getId());
                userUniversityIds.add(note.getCourse().getDepartment().getUniversity().getId());
            }
        });
        userViews.forEach(view -> {
            Note note = noteRepository.findById(view.getNoteId()).orElse(null);
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
        if (ratingsMatrix == null) {
            recomputeRatingsMatrix();
        }

        // Perform SVD on the ratings matrix
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
                .map(Map.Entry::getKey).limit(5)
                .collect(Collectors.toList());

       
        Set<Long> combinedNoteIds = new LinkedHashSet<>();
        //combinedNoteIds.addAll(contentBasedRecommendations.stream().map(Note::getId).collect(Collectors.toList()));
        combinedNoteIds.addAll(svdRecommendedNoteIds);

        List<Note> combinedRecommendations = noteRepository.findAllById(combinedNoteIds);
        return combinedRecommendations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private int findUserIndex(Long userId) {
        return userIndexMap.getOrDefault(userId, -1);
    }

    private Long findNoteId(int index) {
        return indexNoteMap.get(index);
    }*/
    

     public Note updateNote(Long noteId, NoteDTO noteDto) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
        User creator = note.getUser();
        if (noteDto.getCourseId() != null) {
            Course course = courseRepository.findById(noteDto.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid course ID: " + noteDto.getCourseId()));
            note.setCourse(course);
        }
        if (noteDto.getNoteTypeId() != null) {
            NoteType noteType = noteTypeRepository.findById(noteDto.getNoteTypeId()).orElseThrow(() -> new IllegalArgumentException(("Incorrect Note Type Id")) );
            note.setNoteType(noteType);
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
            Optional<Season> seasonOpt = seasonService.getCurrentSeason();
            if(seasonOpt.isPresent() && note.getIsPublic() && !noteDto.getIsPublic()) {
                Season season = seasonOpt.get();

                if (note.getCreatedAt().isAfter(season.getStartDate()) || note.getCreatedAt().isEqual(season.getStartDate())) {

                    UniscoreIncreaseType un = uniscoreIncreaseTypeRepository.findById(23L).orElseThrow(() -> new IllegalArgumentException("Increase type not found"));
                    creator.setSeasonScore(creator.getSeasonScore() - un.getIncreaseAmount());

                    UserSeasonPoints usp = userSeasonPointsRepository.findByIdUserIdAndIdSeasonId(note.getUser().getId(), season.getSeasonId() ).orElseThrow(() -> new IllegalArgumentException("User points not initialized"));
                    usp.setPoints(usp.getPoints() - un.getIncreaseAmount());
                    userSeasonPointsRepository.save(usp);
                    userRepository.save(creator);
                }
            }

            if(seasonOpt.isPresent() && !note.getIsPublic() && noteDto.getIsPublic()) {
                Season season = seasonOpt.get();

                if (note.getCreatedAt().isAfter(season.getStartDate()) || note.getCreatedAt().isEqual(season.getStartDate())) {

                    UniscoreIncreaseType un = uniscoreIncreaseTypeRepository.findById(23L).orElseThrow(() -> new IllegalArgumentException("Increase type not found"));
                    creator.setSeasonScore(creator.getSeasonScore() + un.getIncreaseAmount());

                    UserSeasonPoints usp = userSeasonPointsRepository.findByIdUserIdAndIdSeasonId(note.getUser().getId(), season.getSeasonId() ).orElseThrow(() -> new IllegalArgumentException("User points not initialized"));
                    usp.setPoints(usp.getPoints() + un.getIncreaseAmount());
                    userSeasonPointsRepository.save(usp);
                    userRepository.save(creator);
                }
            }

            note.setIsPublic(noteDto.getIsPublic());    
        }
        if (noteDto.getFilename() != null) {
            note.setFilename(noteDto.getFilename());
        }

        if (noteDto.getProfessor() != null) {
        note.setProfessor(noteDto.getProfessor());
    }

    
    if (noteDto.getAcademicYear() != null) {
        note.setAcademicYear(noteDto.getAcademicYear());
    }

    if (noteDto.getNoteTypeId() != null) {
        NoteType noteType = noteTypeRepository.findById(noteDto.getNoteTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid note type ID: " + noteDto.getNoteTypeId()));
        note.setNoteType(noteType);
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
        
        return likeRepository.existsByNoteIdAndUserIdAndIsActive(noteId, userId);
    }

    public boolean hasUserSaved(Long noteId, Long userId) {
       
        return saveRepository.existsByNoteIdAndUserIdAndIsActive(noteId, userId);
    }
    
        
    @Cacheable("notes")
    public NoteDTO getNoteById(Long id) {
        Note note = noteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Note not found"));
        NoteDTO dto =  convertToDTO(note);
        dto.setSemester(note.getCourse().getSemester());
        dto.setUsername(note.getUser().getUsername());
        dto.setProfileImageUrl(note.getUser().getProfileImageUrl());
        dto.setCertified(note.getUser().getCertified());
        if (note.getNoteType() != null) {
            if (note.getNoteType().getTypeId() != null) {
                dto.setNoteTypeId(note.getNoteType().getTypeId());
            }
            if (note.getNoteType().getTypeName() != null) {
                dto.setNoteType(note.getNoteType().getTypeName());
            }
        }
        
        if (note.getAcademicYear() != null) {
            dto.setAcademicYear(note.getAcademicYear());
        }
        
        if (note.getProfessor() != null) {
            dto.setProfessor(note.getProfessor());
        }
        
        return dto;
    }

    public List<NoteDTO> getNotesByUser(Long userId) {
        return noteRepository.findByUserId(userId);
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

    
    // NEEDS FIXING
    @Transactional
    public void softDeleteNoteById(Long noteId) {
    
    Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid note ID"));
    note.setDeleted(true);
    User user = note.getUser();
    Optional<Season> seasonOpt = seasonService.getCurrentSeason();
        if(seasonOpt.isPresent()) {
            Season season = seasonOpt.get();

            if (note.getCreatedAt().isAfter(season.getStartDate()) || note.getCreatedAt().isEqual(season.getStartDate())) {

                UniscoreIncreaseType un = uniscoreIncreaseTypeRepository.findById(23L).orElseThrow(() -> new IllegalArgumentException("Increase type not found"));
                user.setSeasonScore(user.getSeasonScore() - un.getIncreaseAmount());

                UserSeasonPoints usp = userSeasonPointsRepository.findByIdUserIdAndIdSeasonId(note.getUser().getId(), season.getSeasonId() ).orElseThrow(() -> new IllegalArgumentException("User points not initialized"));
                usp.setPoints(usp.getPoints() - un.getIncreaseAmount());
                userSeasonPointsRepository.save(usp);
                userRepository.save(user);
            }
        }
    noteRepository.save(note);
}
    public Page<NoteDTO> getPublicNotes(int page, int size, String sortBy, String sortDir) {
        
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");            
        validSortFields.put("createdAt", "createdAt");    
        validSortFields.put("title", "title");            
        
        
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                    ? Sort.by(sortField).ascending() 
                    : Sort.by(sortField).descending();
    
        
        Pageable pageable = PageRequest.of(page, size, sort);
    
        
        return noteRepository.findPublicNotes(pageable);
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


    public Page<NoteDTO> getPublicNotesByDepartmentAndSemester(Long departmentId, int semester, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByDepartmentAndSemester(departmentId, semester, pageable);
    }
    
    public Page<NoteDTO> getPublicNotesByDepartment(Department department, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByDepartment(department, pageable);
    }
    


    public Page<NoteDTO> getPublicNotesByCourse(Course course, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByCourse(course, pageable);
    }
    

    public Page<NoteDTO> getPublicNotesByUniversity(University university, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByUniversity(university, pageable);
    }
    

    private NoteDTO convertToDTO(Note note) {
        NoteDTO dto = new NoteDTO(
            note.getId(),
            note.getCourse().getId(),
            note.getUser().getId(),
            note.getTitle(),
            note.getDescription(),
            note.getPdfUrl(),
            note.getFilename(),
            note.getIsPublic()
        );
        
        dto.setTotalLikes(note.getLikes());
        String englishCourseName = note.getCourse().getCourseNames().stream()
            .filter(courseName -> "EN".equals(courseName.getLanguage().getCode()))
            .map(CourseName::getName)
            .findFirst()
            .orElse("Unknown Course Name");  

        dto.setCourseName(englishCourseName);   
        String englishDepartmentName = note.getCourse().getDepartment().getDepartmentNames().stream()
            .filter(departmentName -> "EN".equals(departmentName.getLanguage().getCode()))
            .map(DepartmentName::getName)
            .findFirst()
            .orElse("Unknown Department Name"); 
        dto.setDepartmentName(englishDepartmentName);     

        String englishUniversityName = note.getCourse().getDepartment().getUniversity().getUniversityNames().stream()
            .filter(universityName -> "EN".equals(universityName.getLanguage().getCode()))
            .map(UniversityName::getName)
            .findFirst()
            .orElse("Unknown University Name");  
        dto.setUniversityName(englishUniversityName);
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }


    public Page<NoteDTO> searchNotes(String keyword, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
        return noteRepository.searchNotes(keyword, pageable);
    }

    public Page<NoteDTO> searchUserNotes(String keyword,Long userId, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
        return noteRepository.searchUserNotes(keyword,userId, pageable);
    }

    public Page<NoteDTO> searchUserNotesWithEditDistance(String keyword,Long userId, int threshold, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "like_count");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "like_count");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Object[]> result = noteRepository.searchUserNotesWithEditDistance(keyword, userId,threshold, pageable);
        return result.map(objects -> {
            NoteDTO noteDTO = new NoteDTO();
            noteDTO.setNoteId((Long) objects[0]);
            noteDTO.setCourseId((Long) objects[1]);
            noteDTO.setUserId((Long) objects[2]);
            noteDTO.setTitle((String) objects[3]);
            noteDTO.setDescription((String) objects[4]);
            noteDTO.setPdfUrl((String) objects[5]);
            noteDTO.setFilename((String) objects[6]);
            noteDTO.setIsPublic((Boolean) objects[7]);
            noteDTO.setCourseName((String) objects[8]);
            noteDTO.setUniversityName((String) objects[9]); 
            noteDTO.setDepartmentName((String) objects[10]);
            noteDTO.setTotalLikes((Long) objects[11]);
            noteDTO.setUsername((String) objects[12]);
            noteDTO.setProfileImageUrl((String) objects[13]);
            noteDTO.setCreatedAt((LocalDateTime) objects[14]);
            noteDTO.setProfessor((String) objects[15]);
            noteDTO.setNoteType((String) objects[16]);
            noteDTO.setAcademicYear((String) objects[17]);
            return noteDTO;
        });
    }
    

    public List<NoteDTO> searchNotesWithEditDistance(String keyword, double threshold, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "like_count");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "like_count");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
        int start_row = page*size;
        int end_row =  start_row+size; 
        int isShort = 0;
        if (keyword.length() <= 4) {
            isShort = 1;
        }
        List<Object[]> result = noteRepository.searchNotes(keyword, (float)0.3, start_row, end_row);
        return result.stream().map(objects -> {
            NoteDTO noteDTO = new NoteDTO();
    
            
            noteDTO.setNoteId(Converters.convertToLong(objects[0]));
            noteDTO.setCourseId(Converters.convertToLong(objects[1]));
            noteDTO.setUserId(Converters.convertToLong(objects[2]));
            
            noteDTO.setTitle(Converters.convertToString(objects[3]));
            noteDTO.setDescription(Converters.convertToString(objects[4]));
            noteDTO.setPdfUrl(Converters.convertToString(objects[5]));
            noteDTO.setFilename(Converters.convertToString(objects[6]));
    
            noteDTO.setIsPublic(Converters.convertToBoolean(objects[7]));
    
            noteDTO.setCourseName(Converters.convertToString(objects[8]));
            noteDTO.setUniversityName(Converters.convertToString(objects[9]));
            noteDTO.setDepartmentName(Converters.convertToString(objects[10]));
    
            noteDTO.setTotalLikes(Converters.convertToLong(objects[11]));
    
            noteDTO.setUsername(Converters.convertToString(objects[12]));
            noteDTO.setProfileImageUrl(Converters.convertToString(objects[13]));
    
            noteDTO.setCreatedAt(Converters.convertToLocalDateTime(objects[14]));
    
            noteDTO.setProfessor(Converters.convertToString(objects[15]));
            noteDTO.setAcademicYear(Converters.convertToString(objects[16]));
            noteDTO.setNoteType(Converters.convertToString(objects[17]));
            noteDTO.setCertified(objects[18] != null && ((BigDecimal) objects[18]).intValue() == 1);
            return noteDTO;
        }).collect(Collectors.toList());
    }


    public NoteSearchResponse searchNotesWithEditDistancePaginated(String keyword, double threshold, int page, int size, String sortBy, String sortDir) {
    Map<String, String> validSortFields = new HashMap<>();
    validSortFields.put("likes", "like_count");
    validSortFields.put("createdAt", "createdAt");
    validSortFields.put("title", "title");

    String sortField = validSortFields.getOrDefault(sortBy, "like_count");

    // Create a Sort object for ordering the results
    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
            ? Sort.by(sortField).ascending()
            : Sort.by(sortField).descending();

    Pageable pageable = PageRequest.of(page, size, sort);
    int start_row = page * size;
    int end_row = start_row + size;

    int isShort = 0;
    if (keyword.length() <= 4) {
        isShort = 1;
    }

    // Call the repository method to get the results with the additional fields
    List<Object[]> res = noteRepository.searchNotesWithPagination(keyword, (float) threshold, start_row, end_row);
    List<NoteDTO> dtos = res.stream().map(objects -> {
        NoteDTO noteDTO = new NoteDTO();

        
        noteDTO.setNoteId(Converters.convertToLong(objects[0]));
        noteDTO.setCourseId(Converters.convertToLong(objects[1]));
        noteDTO.setUserId(Converters.convertToLong(objects[2]));
        
        noteDTO.setTitle(Converters.convertToString(objects[3]));
        noteDTO.setDescription(Converters.convertToString(objects[4]));
        noteDTO.setPdfUrl(Converters.convertToString(objects[5]));
        noteDTO.setFilename(Converters.convertToString(objects[6]));

        noteDTO.setIsPublic(Converters.convertToBoolean(objects[7]));

        noteDTO.setCourseName(Converters.convertToString(objects[8]));
        noteDTO.setUniversityName(Converters.convertToString(objects[9]));
        noteDTO.setDepartmentName(Converters.convertToString(objects[10]));

        noteDTO.setTotalLikes(Converters.convertToLong(objects[11]));

        noteDTO.setUsername(Converters.convertToString(objects[12]));
        noteDTO.setProfileImageUrl(Converters.convertToString(objects[13]));

        noteDTO.setCreatedAt(Converters.convertToLocalDateTime(objects[14]));

        noteDTO.setProfessor(Converters.convertToString(objects[15]));
        noteDTO.setAcademicYear(Converters.convertToString(objects[16]));
        noteDTO.setNoteType(Converters.convertToString(objects[17]));
        noteDTO.setCertified(objects[18] != null && ((BigDecimal) objects[18]).intValue() == 1);

        return noteDTO;
    }).collect(Collectors.toList());
   
    long totalElements = 0;
    int totalPages = 0;
    if (!res.isEmpty() && res.get(0).length > 18) { 
        totalElements = Converters.convertToLong(res.get(0)[20]);
        if(start_row < end_row) {
            totalPages = (int) Math.ceil((double) totalElements / size);
        } else {
            totalPages =1;
        }
    }

    
    return new NoteSearchResponse(dtos, totalElements, totalPages);
}


    public Note saveNote(NoteDTO noteDto) {
        Course course = courseRepository.findById(noteDto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));
        User user = userRepository.findById(noteDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        System.out.println(course.toString()+user.toString());
        badgeService.checkSpecialBadgesForUser(noteDto.getUserId(), noteDto.getCourseId());
        Note note = new Note();
        note.setCourse(course);
        note.setUser(user);
        note.setTitle(noteDto.getTitle());
        note.setDescription(noteDto.getDescription());
        note.setPdfUrl(noteDto.getPdfUrl());
        note.setFilename(noteDto.getFilename());
        note.setIsPublic(noteDto.getIsPublic());
        if (noteDto.getProfessor() != null) {
            note.setProfessor(noteDto.getProfessor());
        }
    
        
        if (noteDto.getAcademicYear() != null) {
            note.setAcademicYear(noteDto.getAcademicYear());
        }
    
        if (noteDto.getNoteTypeId() != null) {
            NoteType noteType = noteTypeRepository.findById(noteDto.getNoteTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid note type ID: " + noteDto.getNoteTypeId()));
            note.setNoteType(noteType);
        }

        Note savedNote = noteRepository.save(note);
        boolean hasReceivedFirstLog = uniscoreIncreaseLogsRepository.existsByUserIdAndIncreaseTypeId(user.getId(), 22L);

            if (!hasReceivedFirstLog) {
                
                userService.updateUniScore(user, 22L); 
            } else {
                
                userService.updateUniScore(user, 23L); 
            }
        badgeService.checkBadgesForUser(user.getId()); 
        return savedNote;
    }

    public List<NoteDTO> getPublicSavedNotesByUser(Long userId) {
        return noteRepository.findPublicSavedNotesByUserId(userId);   
    }

    public List<NoteProjection> getTopPublicNotesByUser(Long userId, int limit) {

        return noteRepository.findTopPublicNotesByUser(userId,limit);
    }


    public Optional<Long> findNoteIdByUuid(String uuid) {
        return noteRepository.findIdByUuid(uuid);
    }

    
    public Optional<String> findUuidByNoteId(Long id) {
        return noteRepository.findUuidById(id);
    }


    public Page<NoteDTO> getRecentPublicNotesByDepartment(Department department, int page, int size, String sortBy, String sortDir) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findRecentPublicNotesByDepartment(department,  pageable);
    }

    public Page<NoteDTO> getPublicNotesByDepartmentAndSemesterByType(Long departmentId, int semester, int page, int size, String sortBy, String sortDir, Long typeId) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByDepartmentAndSemesterByType(departmentId, semester, typeId, pageable);
    }
    
    public Page<NoteDTO> getPublicNotesByDepartmentByType(Department department, int page, int size, String sortBy, String sortDir, Long typeId) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByDepartmentByType(department, typeId,pageable);
    }
    


    public Page<NoteDTO> getPublicNotesByCourseByType(Course course, int page, int size, String sortBy, String sortDir, Long typeId) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByCourseByType(course, typeId,pageable);
    }
    

    public Page<NoteDTO> getPublicNotesByUniversityByType(University university, int page, int size, String sortBy, String sortDir, Long typeId) {
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");
        validSortFields.put("createdAt", "createdAt");
        validSortFields.put("title", "title");
    
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
    
        Pageable pageable = PageRequest.of(page, size, sort);
    
        return noteRepository.findPublicNotesByUniversityByType(university,typeId, pageable);
    }


    public Page<NoteDTO> getPublicNotesByType(int page, int size, String sortBy, String sortDir, Long typeId) {
        
        Map<String, String> validSortFields = new HashMap<>();
        validSortFields.put("likes", "likes");            
        validSortFields.put("createdAt", "createdAt");    
        validSortFields.put("title", "title");            
    
        
        String sortField = validSortFields.getOrDefault(sortBy, "likes");
    
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                    ? Sort.by(sortField).ascending() 
                    : Sort.by(sortField).descending();
    
        
        Pageable pageable = PageRequest.of(page, size, sort);
    
        
        return noteRepository.findPublicNotesByType(pageable, typeId);
    }


    public List<CourseNameDTO> getCoursesWithNotesByUser(Long userId) {
        return noteRepository.findCoursesWithNotesByUserId(userId);
    }
}
