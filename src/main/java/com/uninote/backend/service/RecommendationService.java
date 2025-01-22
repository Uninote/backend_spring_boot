package com.uninote.backend.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.uninote.backend.dto.NoteDTO;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.NoteViewRepository;

@Service
public class RecommendationService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteViewRepository noteViewRepository;

    private final ConcurrentHashMap<Long, List<NoteDTO>> recommendationsCache = new ConcurrentHashMap<>();

    private final long MIN_VIEWS = 15L;
    private final long MIN_LIKES = 5L;
    private final long RECENT_DAYS_THRESHOLD = 7L;
    private final double MIN_AVG_VIEW_DURATION = 30;

    private final int AVG_DURATION_WEIGHT = 3;
    private final int VIEW_COUNT_WEIGHT = 2;
    private final int RECENCY_WEIGHT = 1;
    private final int LIKES_WEIGHT = 2;


    @EventListener(ApplicationReadyEvent.class)
    public void initializeRecommendations() {
        computeAndCacheRecommendations();
    }

    private void computeAndCacheRecommendations() {
        List<NoteDTO> updatedRecommendations = reRankAndShuffleNotes();
        recommendationsCache.put(1L, updatedRecommendations); 
    }

    @Scheduled(cron = "0 0 0 * * *") 
    public void scheduledReRanking() {
        List<NoteDTO> updatedRecommendations = reRankAndShuffleNotes();
        recommendationsCache.put(1L, updatedRecommendations); 
    }

    private List<NoteDTO> getModeratedNotes() {
        List<NoteDTO> prolificCreatorNotes = noteRepository.findNotesByGoodCreators(MIN_VIEWS, MIN_LIKES);

        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(RECENT_DAYS_THRESHOLD);
        List<NoteDTO> recentNotes = noteRepository.findRecentNotes(recentThreshold);

        List<NoteDTO> combinedNotes = new ArrayList<>(prolificCreatorNotes);
        combinedNotes.addAll(recentNotes);

        return removeDuplicatesByNoteId(combinedNotes);
    }

    private List<NoteDTO> removeDuplicatesByNoteId(List<NoteDTO> list) {
        Set<Long> seenNoteIds = new HashSet<>();
        return list.stream()
                .filter(note -> seenNoteIds.add(note.getNoteId()))
                .collect(Collectors.toList());
    }

    public List<NoteDTO> filterNotes() {
        List<NoteDTO> moderatedNotes = getModeratedNotes();

        Set<Long> noteWithAVGduration = noteViewRepository.findNotesWithMinAvgViewDuration(MIN_AVG_VIEW_DURATION)
                .stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toSet());

        Set<Long> notesWithFewViews = noteViewRepository.findNotesWithFewViews(3)
                .stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toSet());

        return moderatedNotes.stream()
                .filter(note -> noteWithAVGduration.contains(note.getNoteId()) || notesWithFewViews.contains(note.getNoteId()))
                .collect(Collectors.toList());
    }

    private int calculateNoteScore(NoteDTO note, Set<Long> avgDurationNotes, Set<Long> fewViewNotes) {
        int score = 0;

        if (avgDurationNotes.contains(note.getNoteId())) {
            score += AVG_DURATION_WEIGHT * 10;
        }

        if (fewViewNotes.contains(note.getNoteId())) {
            score += VIEW_COUNT_WEIGHT * 5;
        }

        if (note.getCreatedAt() != null) {
            long daysSinceCreation = ChronoUnit.DAYS.between(note.getCreatedAt(), LocalDateTime.now());
            if (daysSinceCreation <= RECENT_DAYS_THRESHOLD) {
                score += RECENCY_WEIGHT * (RECENT_DAYS_THRESHOLD - daysSinceCreation);
            }
        }

        score += note.getTotalLikes() * LIKES_WEIGHT;
        return score;
    }

    public List<NoteDTO> rankNotes() {
        List<NoteDTO> filteredNotes = filterNotes();

        Set<Long> avgDurationNotes = noteViewRepository.findNotesWithMinAvgViewDuration(MIN_AVG_VIEW_DURATION)
                .stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toSet());

        Set<Long> fewViewNotes = noteViewRepository.findNotesWithFewViews(3)
                .stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toSet());

        return filteredNotes.stream()
                .sorted((note1, note2) -> Integer.compare(
                        calculateNoteScore(note2, avgDurationNotes, fewViewNotes),
                        calculateNoteScore(note1, avgDurationNotes, fewViewNotes)))
                .collect(Collectors.toList());
    }

    public List<NoteDTO> reRankAndShuffleNotes() {
        List<NoteDTO> rankedNotes = rankNotes();
    
        Map<NoteDTO, Integer> noteScores = rankedNotes.stream()
                .collect(Collectors.toMap(
                        note -> note, 
                        note -> calculateNoteScore(note, noteViewRepository.findNotesWithMinAvgViewDuration(MIN_AVG_VIEW_DURATION).stream()
                                .map(result -> (Long) result[0])
                                .collect(Collectors.toSet()),
                            noteViewRepository.findNotesWithFewViews(3L).stream()
                                .limit(50)
                                .map(result -> (Long) result[0])
                                .collect(Collectors.toSet()))
                ));
    
            Map<Integer, List<NoteDTO>> notesByScore = rankedNotes.stream()
                .collect(Collectors.groupingBy(noteScores::get));
    
        List<NoteDTO> shuffledNotes = new ArrayList<>();
        for (List<NoteDTO> scoreGroup : notesByScore.values()) {
            Map<Long, List<NoteDTO>> notesByCourse = scoreGroup.stream()
                    .collect(Collectors.groupingBy(NoteDTO::getCourseId));
    
            List<NoteDTO> shuffledScoreGroup = new ArrayList<>();
            for (List<NoteDTO> courseGroup : notesByCourse.values()) {
                Map<Long, List<NoteDTO>> notesByCreator = courseGroup.stream()
                        .collect(Collectors.groupingBy(NoteDTO::getUserId));
    
                List<NoteDTO> shuffledCourseGroup = new ArrayList<>();
                for (List<NoteDTO> creatorGroup : notesByCreator.values()) {
                    Collections.shuffle(creatorGroup);
                    shuffledCourseGroup.addAll(creatorGroup);
                }
    
                Collections.shuffle(shuffledCourseGroup);
                shuffledScoreGroup.addAll(shuffledCourseGroup);
            }
    
            Collections.shuffle(shuffledScoreGroup);
            shuffledNotes.addAll(shuffledScoreGroup);
        }
    
        shuffledNotes.sort(Comparator.comparingInt(noteScores::get).reversed(   ));
    
        return shuffledNotes;
    }

    public List<NoteDTO> getCachedRecommendations() {
        return recommendationsCache.get(1L);
    }
    
    
    
}
