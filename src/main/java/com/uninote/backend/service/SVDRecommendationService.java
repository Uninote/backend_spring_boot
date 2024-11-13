package com.uninote.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.entity.NoteInteraction;
import com.uninote.backend.entity.NoteLike;
import com.uninote.backend.entity.NoteSave;
import com.uninote.backend.entity.NoteView;
import com.uninote.backend.repository.NoteLikeRepository;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteSaveRepository;
import com.uninote.backend.repository.NoteViewRepository;
import com.uninote.backend.repository.UserRepository;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class SVDRecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(SVDRecommendationService.class);

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

    private RealMatrix userNoteMatrix;     
    private RealMatrix[] svdMatrices;      
    private List<Long> userIds;            
    private List<Long> noteIds;            

    @Scheduled(cron = "0 0 0 * * *")  // Runs daily at midnight
    public void refreshUserNoteMatrix() {
        logger.info("Starting refresh of user-note interaction matrix and SVD.");

        try {
            userIds = getAllUserIds();
            noteIds = getAllNoteIds();
            logger.info("Retrieved {} users and {} notes for matrix computation", userIds.size(), noteIds.size());

            // Fetch all interactions once and store in maps
            Map<Long, Set<Long>> likesMap = getUserLikesMap();
            Map<Long, Set<Long>> savesMap = getUserSavesMap();
            Map<Long, Map<Long, Integer>> viewsMap = getUserViewsMap();

            userNoteMatrix = buildUserNoteMatrix(userIds, noteIds, likesMap, savesMap, viewsMap);
            svdMatrices = performSVD(userNoteMatrix); 
            logger.info("Successfully refreshed user-note matrix and SVD matrices");
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
                
                // Calculate the interaction score based on fetched maps
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

    public List<NoteDTO> getRecommendationsForUser(Long userId) {
        logger.info("Generating recommendations for user ID: {}", userId);

        if (userNoteMatrix == null || svdMatrices == null || userIds == null || noteIds == null) {
            logger.warn("User-note matrix or SVD matrices not initialized; refreshing matrices");
            refreshUserNoteMatrix(); 
        }

        int userIndex = userIds.indexOf(userId);
        if (userIndex == -1) {
            logger.warn("User ID {} not found in user-note matrix", userId);
            return Collections.emptyList();
        }

        RealMatrix userFeatures = svdMatrices[0].getRowMatrix(userIndex).multiply(svdMatrices[1]);
        RealMatrix predictedScores = userFeatures.multiply(svdMatrices[2].transpose());

        Map<Long, Double> noteScores = new HashMap<>();
        for (int i = 0; i < noteIds.size(); i++) {
            noteScores.put(noteIds.get(i), predictedScores.getEntry(0, i));
        }

        List<Long> topNoteIds = noteScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());

        logger.info("Top recommended notes for user ID {}: {}", userId, topNoteIds);
        return noteRepository.findNotesByIds(topNoteIds);
    }

    private List<Long> getAllUserIds() {
        return userRepository.findUserIds();
    }

    private List<Long> getAllNoteIds() {
        return noteRepository.findNonDeletedNoteIds();
    }
}
