package com.uninote.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.NoteView;
import com.uninote.backend.entity.UserNoteMatrixEntry;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.NoteViewRepository;
import com.uninote.backend.repository.UserNoteMatrixRepository;
import com.uninote.backend.repository.UserRepository;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import javax.annotation.PostConstruct;


@Service
public class SVDRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(SVDRecommendationService.class);

    @Autowired
    private UserNoteMatrixRepository userNoteMatrixRepository;

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NoteLikeRepository noteLikeRepository;
    @Autowired
    private NoteSaveRepository noteSaveRepository;
    @Autowired
    private NoteViewRepository noteViewRepository;

    @Autowired
    private RecommendationService recommendationService;

    private RealMatrix userNoteMatrix;
    private RealMatrix[] svdMatrices;
    private List<Long> userIds;
    private List<Long> noteIds;

    private Map<Long, Set<Long>> likesMap; 
    private Map<Long, Set<Long>> savesMap; 
    private Map<Long, Map<Long, Integer>> viewsMap; 

    private boolean matrixLoaded = false;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public synchronized void initializeUserNoteMatrix() {
        if (matrixLoaded) {
            logger.info("User-note matrix already loaded, skipping initialization.");
            return;
        }

        userIds = getAllUserIds();
        noteIds = getAllNoteIds();

        if (userNoteMatrixRepository.count() > 0) {
            logger.info("Loading existing user-note matrix from the database.");
            userNoteMatrix = loadUserNoteMatrix(userIds, noteIds);
        } else {
            logger.info("No existing matrix found. Building new user-note matrix.");
            refreshUserNoteMatrix();  
            saveUserNoteMatrix(userNoteMatrix, userIds, noteIds);
        }

        svdMatrices = performSVD(userNoteMatrix);
        matrixLoaded = true;
        likesMap = getUserLikesMap();
        savesMap = getUserSavesMap();
        viewsMap = getUserViewsMap();
    }

    @Scheduled(cron = "0 0 0 * * *")  
    public void refreshUserNoteMatrix() {
        logger.info("Starting refresh of user-note interaction matrix and SVD.");

        try {
            userIds = getAllUserIds();
            noteIds = getAllNoteIds();
            logger.info("Retrieved {} users and {} notes for matrix computation", userIds.size(), noteIds.size());


            likesMap = getUserLikesMap();
            savesMap = getUserSavesMap();
            viewsMap = getUserViewsMap();

            userNoteMatrix = buildUserNoteMatrix(userIds, noteIds, likesMap, savesMap, viewsMap);
            svdMatrices = performSVD(userNoteMatrix);
            logger.info("Successfully refreshed user-note matrix and SVD matrices");
            matrixLoaded = true;    
        } catch (Exception e) {
            logger.error("Error during scheduled matrix refresh", e);
        }
    }

    private RealMatrix buildUserNoteMatrix(List<Long> userIds, List<Long> noteIds,
                                           Map<Long, Set<Long>> likesMap,
                                           Map<Long, Set<Long>> savesMap,
                                           Map<Long, Map<Long, Integer>> viewsMap) {
        logger.info("Building user-note interaction matrix");

        int numUsers = userIds.size();
        int numNotes = noteIds.size();
        double[][] matrixData = new double[numUsers][numNotes];

        IntStream.range(0, numUsers).parallel().forEach(i -> {
            Long userId = userIds.get(i);

            for (int j = 0; j < numNotes; j++) {
                Long noteId = noteIds.get(j);

                double score = (likesMap.getOrDefault(userId, Collections.emptySet()).contains(noteId) ? 3 : 0) +
                               (savesMap.getOrDefault(userId, Collections.emptySet()).contains(noteId) ? 2 : 0) +
                               viewsMap.getOrDefault(userId, Collections.emptyMap()).getOrDefault(noteId, 0);

                matrixData[i][j] = score;
            }
        });

        logger.info("User-note interaction matrix built with dimensions: {}x{}", numUsers, numNotes);
        return MatrixUtils.createRealMatrix(matrixData);
    }

    private Map<Long, Set<Long>> getUserLikesMap() {
        logger.info("Fetching all user likes in bulk");
        return noteLikeRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.groupingBy(
                        noteLike -> noteLike.getUser().getId(),
                        Collectors.mapping(noteLike -> noteLike.getNote().getId(), Collectors.toSet())
                ));
    }

    private Map<Long, Set<Long>> getUserSavesMap() {
        logger.info("Fetching all user saves in bulk");
        return noteSaveRepository.findAllByIsActiveTrue().stream()
                .collect(Collectors.groupingBy(
                        NoteSave::getUserId,
                        Collectors.mapping(NoteSave::getNoteId, Collectors.toSet())
                ));
    }

    private Map<Long, Map<Long, Integer>> getUserViewsMap() {
        logger.info("Fetching all user views in bulk");

        return noteViewRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        NoteView::getUserId,
                        Collectors.groupingBy(
                                NoteView::getNoteId,
                                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                        )
                ));
    }

    private RealMatrix[] performSVD(RealMatrix userNoteMatrix) {
        logger.debug("Performing SVD on user-note matrix");
        SingularValueDecomposition svd = new SingularValueDecomposition(userNoteMatrix);
        return new RealMatrix[]{svd.getU(), svd.getS(), svd.getV()};
    }

    public List<NoteDTO> getRecommendationsForUser(Long userId, String languageCode) {
        logger.info("Generating recommendations for user ID: {}", userId);

        if (userNoteMatrix == null || svdMatrices == null || userIds == null || noteIds == null) {
            logger.warn("User-note matrix or SVD matrices not initialized; refreshing matrices");
            new Thread(this::initializeUserNoteMatrix).start(); 
            return recommendationService.getCachedRecommendations();
        }

        int userIndex = userIds.indexOf(userId);
        List<Long> recommendedNoteIds;

        if (userIndex == -1 || isUserWithoutData(userIndex)) {
            logger.info("User has no data; returning popular notes based on interactions.");
            recommendedNoteIds = getTopInteractedNotesFromMaps(10);
        } else {
            recommendedNoteIds = getTopRecommendedNoteIds(userIndex, 10);
        }

        return noteRepository.findNotesByIds(recommendedNoteIds, languageCode);
    }

    private boolean isUserWithoutData(int userIndex) {
        return Arrays.stream(userNoteMatrix.getRow(userIndex)).allMatch(score -> score == 0.0);
    }

    private List<Long> getTopRecommendedNoteIds(int userIndex, int limit) {
        RealMatrix userFeatures = svdMatrices[0].getRowMatrix(userIndex).multiply(svdMatrices[1]);
        RealMatrix predictedScores = userFeatures.multiply(svdMatrices[2].transpose());

        Map<Long, Double> noteScores = new HashMap<>();
        for (int i = 0; i < noteIds.size(); i++) {
            noteScores.put(noteIds.get(i), predictedScores.getEntry(0, i));
        }

        return noteScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Long> getTopInteractedNotesFromMaps(int limit) {
        logger.info("Calculating top-interacted notes from in-memory data");

        Map<Long, Integer> noteInteractionScores = new HashMap<>();

        for (Long noteId : noteIds) {
            int score = 0;

            for (Set<Long> likedNotes : likesMap.values()) {
                if (likedNotes.contains(noteId)) score += 3;
            }

            for (Set<Long> savedNotes : savesMap.values()) {
                if (savedNotes.contains(noteId)) score += 2;
            }

            for (Map<Long, Integer> userViews : viewsMap.values()) {
                score += userViews.getOrDefault(noteId, 0);
            }

            noteInteractionScores.put(noteId, score);
        }

        return noteInteractionScores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Long> getAllUserIds() {
        return userRepository.findUserIds();
    }

    private List<Long> getAllNoteIds() {
        return noteRepository.findNonDeletedNoteIds();
    }



    public RealMatrix loadUserNoteMatrix(List<Long> userIds, List<Long> noteIds) {
        double[][] matrixData = new double[userIds.size()][noteIds.size()];  // Initialized to zeros

        List<UserNoteMatrixEntry> entries = userNoteMatrixRepository.findAll();
        Map<Long, Integer> userIndexMap = IntStream.range(0, userIds.size())
                .boxed()
                .collect(Collectors.toMap(userIds::get, i -> i));
        Map<Long, Integer> noteIndexMap = IntStream.range(0, noteIds.size())
                .boxed()
                .collect(Collectors.toMap(noteIds::get, i -> i));

        for (UserNoteMatrixEntry entry : entries) {
            Integer userIndex = userIndexMap.get(entry.getUserId());
            Integer noteIndex = noteIndexMap.get(entry.getNoteId());

            if (userIndex != null && noteIndex != null) {
                matrixData[userIndex][noteIndex] = entry.getInteractionScore();
            } else {
                logger.warn("User ID {} or Note ID {} not found in index maps.", entry.getUserId(), entry.getNoteId());
            }
        }

        return MatrixUtils.createRealMatrix(matrixData);
    }


    public void saveUserNoteMatrix(RealMatrix matrix, List<Long> userIds, List<Long> noteIds) {
        logger.info("Clearing previous entries and saving the user-note interaction matrix to the database.");
        userNoteMatrixRepository.deleteAll();
        List<UserNoteMatrixEntry> entries = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            for (int j = 0; j < noteIds.size(); j++) {
                Long noteId = noteIds.get(j);
                double score = matrix.getEntry(i, j);

                if (score != 0.0) {  
                    UserNoteMatrixEntry entry = new UserNoteMatrixEntry(userId, noteId, score);
                    entries.add(entry);
                }
            }
        }

        if (!entries.isEmpty()) {
            userNoteMatrixRepository.saveAll(entries);
            logger.info("Successfully saved {} non-zero entries to the user-note matrix table.", entries.size());
        } else {
            logger.warn("No non-zero entries to save to the user-note matrix table.");
        }
    }
}
