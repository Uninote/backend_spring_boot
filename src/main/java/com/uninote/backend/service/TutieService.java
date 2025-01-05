package com.uninote.backend.service;

import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.ProcessedNote;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.ProcessedNoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class TutieService {

    private static final Logger logger = LoggerFactory.getLogger(TutieService.class);

    private final ConcurrentLinkedQueue<Long> noteProcessingQueue = new ConcurrentLinkedQueue<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_BASE_URL = "https://external-api.com";
    private boolean isProcessing = false;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ProcessedNoteRepository processedNoteRepository;

    
    @PostConstruct
    public void initQueueOnStartup() {
        List<Note> processingNotes = noteRepository.findByStatus("PROCESSING");
        for (Note note : processingNotes) {
            note.setStatus("FAILED");
            noteRepository.save(note);
            logger.warn("Marked Note ID {} as FAILED (was stuck in PROCESSING state).", note.getId());
        }

        List<Note> pendingNotes = noteRepository.findByStatus("PENDING");
        List<Note> failedNotes = noteRepository.findByStatus("FAILED");

        for (Note note : pendingNotes) {
            noteProcessingQueue.add(note.getId());
        }

        for (Note note : failedNotes) {
            noteProcessingQueue.add(note.getId());
        }

        if (!noteProcessingQueue.isEmpty()) {
            logger.info("Found {} notes to process (PENDING: {}, FAILED: {}). Starting processing...",
                    noteProcessingQueue.size(), pendingNotes.size(), failedNotes.size());
            processQueue();
        }
    }

    
    private synchronized void processQueue() {
        if (isProcessing) {
            return;
        }

        isProcessing = true;

        new Thread(() -> {
            try {
                while (!noteProcessingQueue.isEmpty()) {
                    Long noteId = noteProcessingQueue.poll();
                    if (noteId == null) {
                        continue;
                    }

                    processNoteById(noteId);
                }
            } finally {
                isProcessing = false;
                logger.info("Queue processing completed.");
            }
        }).start();
    }

    
    private void processNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.warn("Note ID {} not found in the database.", noteId);
            return;
        }

        try {
            note.setStatus("PROCESSING");
            noteRepository.save(note);

            NoteProcessingResult result = fetchSummaryAndQuizzes(noteId);

            storeInDatabase(noteId, result);

            note.setStatus("PROCESSED");
            noteRepository.save(note);

            logger.info("Successfully processed Note ID {}", noteId);

        } catch (Exception e) {
            logger.error("Failed to process Note ID {}: {}", noteId, e.getMessage(), e);

            note.setStatus("FAILED");
            noteRepository.save(note);
        }
    }

    
    public void addNoteForProcessing(Long noteId) {
        if (noteId == null) {
            logger.warn("Cannot add null note ID to the processing queue.");
            return;
        }

        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.warn("Note ID {} not found.", noteId);
            return;
        }

        noteProcessingQueue.add(noteId);

        note.setStatus("PENDING");
        noteRepository.save(note);

        logger.info("Note ID {} added to the processing queue.", noteId);

        if (!isProcessing) {
            processQueue();
        }
    }

    
    private NoteProcessingResult fetchSummaryAndQuizzes(Long noteId) {
        String url = UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .path("/processNote")
                .queryParam("noteId", noteId)
                .toUriString();

        logger.info("Fetching summary and quizzes for Note ID {}", noteId);
        return restTemplate.getForObject(url, NoteProcessingResult.class);
    }

    
    private void storeInDatabase(Long noteId, NoteProcessingResult result) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.error("Note ID {} not found for storing processed data.", noteId);
            return;
        }

        ProcessedNote processedNote = new ProcessedNote();
        processedNote.setNote(note);
        processedNote.setSummary(result.getSummary());
        processedNote.setQuizJson(result.getQuizJson());
        processedNoteRepository.save(processedNote);

        logger.info("Stored processed data for Note ID {}", noteId);
    }

    
    public boolean hasNotesToProcess() {
        return !noteProcessingQueue.isEmpty();
    }


    public static class NoteProcessingResult {
        private String summary;
        private String quizJson;

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getQuizJson() {
            return quizJson;
        }

        public void setQuizJson(String quizJson) {
            this.quizJson = quizJson;
        }
    }
}
