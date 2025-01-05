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

    /**
     * Initializes the service on application startup.
     * Handles notes stuck in PROCESSING state and reloads PENDING/FAILED notes into the queue.
     */
    @PostConstruct
    public void initQueueOnStartup() {
        // Handle notes stuck in PROCESSING state
        List<Note> processingNotes = noteRepository.findByStatus("PROCESSING");
        for (Note note : processingNotes) {
            note.setStatus("FAILED");
            noteRepository.save(note);
            logger.warn("Marked Note ID {} as FAILED (was stuck in PROCESSING state).", note.getId());
        }

        // Load PENDING and FAILED notes into the queue
        List<Note> pendingNotes = noteRepository.findByStatus("PENDING");
        List<Note> failedNotes = noteRepository.findByStatus("FAILED");

        for (Note note : pendingNotes) {
            noteProcessingQueue.add(note.getId());
        }

        for (Note note : failedNotes) {
            noteProcessingQueue.add(note.getId());
        }

        // Start processing if there are notes in the queue
        if (!noteProcessingQueue.isEmpty()) {
            logger.info("Found {} notes to process (PENDING: {}, FAILED: {}). Starting processing...",
                    noteProcessingQueue.size(), pendingNotes.size(), failedNotes.size());
            processQueue();
        }
    }

    /**
     * Processes all notes in the queue sequentially.
     * Runs in a separate thread to avoid blocking the main application.
     */
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

    /**
     * Processes a single note by ID.
     * Updates the note's status and handles API calls and database updates.
     */
    private void processNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.warn("Note ID {} not found in the database.", noteId);
            return;
        }

        try {
            // Update note status to PROCESSING
            note.setStatus("PROCESSING");
            noteRepository.save(note);

            // Fetch processed data from external API
            NoteProcessingResult result = fetchSummaryAndQuizzes(noteId);

            // Store processed data in the database
            storeInDatabase(noteId, result);

            // Update note status to PROCESSED
            note.setStatus("PROCESSED");
            noteRepository.save(note);

            logger.info("Successfully processed Note ID {}", noteId);

        } catch (Exception e) {
            logger.error("Failed to process Note ID {}: {}", noteId, e.getMessage(), e);

            // Mark note as FAILED
            note.setStatus("FAILED");
            noteRepository.save(note);
        }
    }

    /**
     * Adds a note to the processing queue and updates its status to PENDING.
     */
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

    /**
     * Fetches the summary and quizzes for a note using an external API.
     */
    private NoteProcessingResult fetchSummaryAndQuizzes(Long noteId) {
        String url = UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .path("/processNote")
                .queryParam("noteId", noteId)
                .toUriString();

        logger.info("Fetching summary and quizzes for Note ID {}", noteId);
        return restTemplate.getForObject(url, NoteProcessingResult.class);
    }

    /**
     * Stores the processed summary and quizzes in the database.
     */
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

    /**
     * Checks if there are notes left in the queue.
     */
    public boolean hasNotesToProcess() {
        return !noteProcessingQueue.isEmpty();
    }

    /**
     * Represents the result of processing a note.
     */
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
