package com.uninote.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.api.gax.rpc.InvalidArgumentException;
import com.uninote.backend.entity.Note;
import com.uninote.backend.entity.ProcessedNote;
import com.uninote.backend.repository.NoteRepository;
import com.uninote.backend.repository.ProcessedNoteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class TutieService {

    private static final Logger logger = LoggerFactory.getLogger(TutieService.class);
    
    private final ConcurrentLinkedQueue<Long> noteProcessingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, Long> taskNoteMap = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_BASE_URL = "https://uninote-tutie-95d7811add59.herokuapp.com/";
    private boolean isProcessing = false;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ProcessedNoteRepository processedNoteRepository;



    @Autowired
    private TokenQuotaService tokenQuotaService;

    private final ConcurrentHashMap<String, Long> sessionMap = new ConcurrentHashMap<>();

    
    @EventListener(ApplicationReadyEvent.class)
    public void initQueueOnStartup() {
        

        List<Note> pendingNotes = noteRepository.findByStatus("PENDING");
        List<Note> failedNotes = noteRepository.findByStatus("FAILED");
        failedNotes.addAll(noteRepository.findByStatus("PROCESSING"));

        pendingNotes.forEach(note -> noteProcessingQueue.add(note.getId()));
        failedNotes.forEach(note -> noteProcessingQueue.add(note.getId()));
            
        if (!noteProcessingQueue.isEmpty()) {
            logger.info("Found {} notes to process (PENDING: {}, FAILED: {}). Starting processing...",
                    noteProcessingQueue.size(), pendingNotes.size(), failedNotes.size());
            processQueue();
        }
    }




    public String getSummaryByNoteId(Long noteId) {
        try {
            ProcessedNote processedNote = processedNoteRepository.findByNoteId(noteId);
            if (processedNote == null) {
                logger.warn("No processed data found for Note ID {}", noteId);
                return null;
            }
            logger.info("Retrieved summary for Note ID {}", noteId);
            return processedNote.getSummary();
        } catch (Exception e) {
            logger.error("Error retrieving summary for Note ID {}: {}", noteId, e.getMessage(), e);
            return null;
        }
    }

    
    public String getQuizzesByNoteId(Long noteId) {
        try {
            ProcessedNote processedNote = processedNoteRepository.findByNoteId(noteId);
            if (processedNote == null) {
                logger.warn("No processed data found for Note ID {}", noteId);
                return null;
            }
            logger.info("Retrieved quizzes for Note ID {}", noteId);
            return processedNote.getQuizJson();
        } catch (Exception e) {
            logger.error("Error retrieving quizzes for Note ID {}: {}", noteId, e.getMessage(), e);
            return null;
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

                    processNoteViaCelery(noteId);
                    String taskId = findTaskIdForNoteId(noteId);

                if (taskId != null) {
                    waitForTaskCompletion(taskId);
                } else {
                    logger.warn("Task ID not found for Note ID {}", noteId);
                }
                }
            } finally {
                isProcessing = false;
                logger.info("Queue processing completed.");
            }
        }).start();
    }

    
    private String findTaskIdForNoteId(Long noteId) {
        return taskNoteMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(noteId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
    
    

    
    private void waitForTaskCompletion(String taskId) {
        boolean isCompleted = false;
    
        while (!isCompleted) {
            try {
                logger.info("Checking status for Task ID {}", taskId);
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        API_BASE_URL + "/process-status/" + taskId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                );
    
                if (response.getBody() != null) {
                    String status = (String) response.getBody().get("status");
    
                    switch (status.toUpperCase()) {
                        case "SUCCESS":
                            Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                            processTaskResult(taskNoteMap.get(taskId), result);
                            taskNoteMap.remove(taskId);
                            isCompleted = true;
                            logger.info("Task ID {} completed successfully.", taskId);
                            break;
    
                        case "FAILURE":
                            logger.error("Task ID {} failed. Traceback: {}", taskId, response.getBody().get("traceback"));
                            Note note = noteRepository.findById(taskNoteMap.get(taskId)).orElseThrow(() -> new IllegalArgumentException("Note not found"));
                            markNoteAsNonDigitizable(note, "Task failed.");
                            taskNoteMap.remove(taskId);
                            isCompleted = true;
                            break;
    
                        default:
                            logger.info("Task ID {} is still in progress.", taskId);
                            Thread.sleep(5000); // Wait for 5 seconds before rechecking
                            break;
                    }
                }
            } catch (Exception e) {
                logger.error("Error while checking status for Task ID {}: {}", taskId, e.getMessage(), e);
                try {
                    Thread.sleep(5000); // Wait before retrying in case of error
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
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
    
            if (result == null || "NOT_DIGITIZABLE".equals(result.getStatus())) {
                String reason = result != null ? result.getMessage() : "No additional details provided.";
                logger.warn("Note ID {} cannot be digitized. Reason: {}", noteId, reason);
                markNoteAsNonDigitizable(note, reason);
                return;
            }
    
            storeInDatabase(noteId, result);
    
            note.setStatus("PROCESSED");
            noteRepository.save(note);
    
            logger.info("Successfully processed Note ID {}", noteId);
    
        } catch (Exception e) {
            logger.error("Failed to process Note ID {}: {}", noteId, e.getMessage(), e);
            markNoteAsNonDigitizable(note, "Processing error: " + e.getMessage());
        }
    }
    
    
    private void markNoteAsNonDigitizable(Note note, String reason) {
        note.setStatus("NON_DIGITIZABLE");
        noteRepository.save(note);
        logger.warn("Marked Note ID {} as NON_DIGITIZABLE: {}", note.getId(), reason);
    }
    

    public void processNoteViaCelery(Long noteId) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.warn("Note ID {} not found in the database.", noteId);
            return;
        }
    
        try {
            note.setStatus("PROCESSING");
            noteRepository.save(note);
            
            String url = API_BASE_URL + "/process";
            Map<String, Long> requestBody = new HashMap<>();
            requestBody.put("note_id", noteId);
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Long>> requestEntity = new HttpEntity<>(requestBody, headers);
    
            logger.info("Submitting Note ID {} to Celery for processing.", noteId);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
    
            if (response.getBody() != null && response.getBody().containsKey("task_id")) {
                String taskId = (String) response.getBody().get("task_id");
    
                taskNoteMap.put(taskId, noteId);
    
                logger.info("Mapped Celery Task ID to Note ID {}: {}", noteId, taskId);
            } else {
                logger.error("Failed to retrieve Task ID for Note ID {}.", noteId);
                markNoteAsNonDigitizable(note, "Failed to retrieve Task ID from Celery.");
            }
        } catch (Exception e) {
            logger.error("Error submitting Note ID {} to Celery: {}", noteId, e.getMessage(), e);
            markNoteAsNonDigitizable(note, "Error submitting task to Celery.");
        }
    }
    
     public String uploadNoteFile(MultipartFile file, Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_BASE_URL + "/upload-note",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ("success".equalsIgnoreCase((String) responseBody.get("status"))) {
                    String sessionId = (String) responseBody.get("session_id");
                    sessionMap.put(sessionId, userId);
                    logger.info("File uploaded successfully. Session ID: {}", sessionId);
                    return sessionId;
                } else {
                    logger.error("File upload failed. Response: {}", responseBody);
                    throw new RuntimeException("File upload failed: " + responseBody);
                }
            } else {
                logger.error("Unexpected response status: {}", response.getStatusCode());
                throw new RuntimeException("Unexpected response status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error occurred while uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error occurred while uploading file: " + e.getMessage(), e);
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
        String path = String.format("%s","/process");
        String url = UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .path(path)
                .toUriString();
    
        try {
            logger.info("Fetching summary and quizzes for Note ID {}", noteId);
            Map<String, Long> requestBody = new HashMap<>();
                requestBody.put("note_id", noteId);
            ResponseEntity<NoteProcessingResult> response = restTemplate.postForEntity(
                url,
                requestBody,
                NoteProcessingResult.class
        );
    
            if (response.getBody() == null) {
                logger.warn("Received empty response for Note ID {}", noteId);
                return null;
            }
    
            NoteProcessingResult result = response.getBody();
    
            if ("NOT_DIGITIZABLE".equals(result.getStatus())) {
                logger.warn("Note ID {} cannot be digitized. Reason: {}", noteId, result.getMessage());
            }
    
            return result;
        } catch (Exception e) {
            logger.error("Error fetching summary and quizzes for Note ID {}: {}", noteId, e.getMessage(), e);
            return null;
        }
    }
    

    private void processTaskResult(Long noteId, Map<String, Object> result) {
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            logger.error("Note ID {} not found in the database while processing result.", noteId);
            return;
        }
    
        try {
            String status = (String) result.get("status");
            if ("NOT_DIGITIZABLE".equalsIgnoreCase(status)) {
                String reason = (String) result.getOrDefault("message", "No additional details provided.");
                logger.warn("Note ID {} cannot be digitized. Reason: {}", noteId, reason);
                markNoteAsNonDigitizable(note, reason);
                return;
            }
    
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data == null) {
                logger.warn("Task result data is missing for Note ID {}.", noteId);
                markNoteAsNonDigitizable(note, "Task result contained no data.");
                return;
            }
    
            String summary = (String) data.getOrDefault("summary", "No summary available.");
            List<Map<String, Object>> quizzes = (List<Map<String, Object>>) data.getOrDefault("quizJson", new ArrayList<>());
            String text = (String) data.getOrDefault("text", "No text available.");
            if (summary.isEmpty() && quizzes.isEmpty()) {
                logger.warn("No meaningful data found in the task result for Note ID {}.", noteId);
                markNoteAsNonDigitizable(note, "Task result contained no meaningful data.");
                return;
            }
    
            NoteProcessingResult noteProcessingResult = new NoteProcessingResult();
            noteProcessingResult.setQuizJson(quizzes);
            noteProcessingResult.setSummary(summary);
            noteProcessingResult.setText(text);
            storeInDatabase(noteId, noteProcessingResult);
    
            note.setStatus("PROCESSED");
            noteRepository.save(note);
    
            logger.info("Successfully processed and stored data for Note ID {}", noteId);
    
        } catch (Exception e) {
            logger.error("Error processing result for Note ID {}: {}", noteId, e.getMessage(), e);
            markNoteAsNonDigitizable(note, "Processing error: " + e.getMessage());
        }
    }
    
    

    
    private void storeInDatabase(Long noteId, NoteProcessingResult result) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note == null) {
                logger.error("Note ID {} not found for storing processed data.", noteId);
                return;
            }

            ProcessedNote processedNote = new ProcessedNote();
            processedNote.setNote(note);
            processedNote.setSummary(result.getSummary());
            ObjectMapper objectMapper = new ObjectMapper();
            String quizJsonString = objectMapper.writeValueAsString(result.getQuizJson());
            processedNote.setQuizJson(quizJsonString);
            processedNote.setText(result.getText());
            processedNoteRepository.save(processedNote);

            logger.info("Stored processed data for Note ID {}", noteId);
        } catch (Exception e) {
            logger.error("Error storing processed data for Note ID {}: {}", noteId, e.getMessage(), e);
        }
    }

    
    public boolean hasNotesToProcess() {
        return !noteProcessingQueue.isEmpty();
    }


    @Scheduled(fixedRate = 3000) // Run every 30 seconds
    public void checkPendingTaskStatuses() {
        logger.info("Checking status for pending tasks...");

        for (String taskId : taskNoteMap.keySet()) {
            checkTaskStatus(taskId);
        }
    }

    public void checkTaskStatus(String taskId) {
        Long noteId = taskNoteMap.get(taskId);
        if (noteId == null) {
            logger.warn("No note found for Task ID {}.", taskId);
            return;
        }
    
        String taskStatusUrl = API_BASE_URL + "/task-status?task_id=" + taskId;
    
        try {
            logger.info("Checking status for Celery Task ID: {}", taskId);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    taskStatusUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
    
            if (response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                if ("SUCCESS".equalsIgnoreCase(status)) {
                    Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
                    processTaskResult(noteId, result);
                    taskNoteMap.remove(taskId);
                    logger.info("Successfully processed Task ID {} for Note ID {}", taskId, noteId);
                } else if ("FAILURE".equalsIgnoreCase(status)) {
                    logger.error("Celery task failed for Note ID {}: {}", noteId, response.getBody().get("traceback"));
                    Note note = noteRepository.findById(noteId).orElse(null);
                    markNoteAsNonDigitizable(note, "Task failed");
                    taskNoteMap.remove(taskId);
                } else {
                    logger.info("Task ID {} is still in progress.", taskId);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking status for Task ID {}: {}", taskId, e.getMessage(), e);
        }
    }
    


    public Map<String, Object> getAnswerAndRelatedNotes(String userPrompt, Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("question", userPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            String url = API_BASE_URL + "/answer-question";
            logger.info("Sending user prompt to external service: {}", userPrompt);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            logger.info(jsonResponse.asText());

            String answer = jsonResponse.get("answer").asText();
            List<Long> noteIds = new ArrayList<>();
            jsonResponse.get("note_ids").forEach(id -> noteIds.add(id.asLong()));
            int totalTokens = jsonResponse.get("total_tokens").asInt();
            tokenQuotaService.updateTokenUsage(userId, totalTokens);
            result.put("answer", answer);
            result.put("noteIds", noteIds);
            logger.info("Received response from external service: Answer={}, Note IDs={}", answer, noteIds);
        } catch (Exception e) {
            logger.error("Error communicating with external service: {}", e.getMessage(), e);
            result.put("error", "Failed to retrieve data from external service.");
        }
        return result;
    }



    public Map<String, String> getSummaryAndQuizzesByNoteId(Long noteId) {
        Map<String, String> result = new HashMap<>();
        try {
            ProcessedNote processedNote = processedNoteRepository.findByNoteId(noteId);
    
            if (processedNote == null) {
                logger.warn("No processed data found for Note ID {}", noteId);
                return null; 
            }
    
            result.put("summary", processedNote.getSummary() != null ? processedNote.getSummary() : "No summary available");
            result.put("quizzes", processedNote.getQuizJson() != null ? processedNote.getQuizJson() : "No quizzes available");
    
            logger.info("Successfully retrieved summary and quizzes for Note ID {}", noteId);
            return result;
    
        } catch (Exception e) {
            logger.error("Error retrieving summary and quizzes for Note ID {}: {}", noteId, e.getMessage(), e);
            throw new RuntimeException("Unable to fetch summary and quizzes for Note ID: " + noteId, e);
        }
    }

    public int calculateTokens(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String API_URL = API_BASE_URL + "/calculate_tokens";
            Map<String, String> payload = Map.of(
                "prompt", prompt,
                "model", "gpt-4" 
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return (int) response.getBody().get("token_count");
        } catch (Exception e) {
            e.printStackTrace();
            return -1; 
        }
    }
    


    public Map<String, Object> handleChat(String sessionId, String message) {
        String fastApiUrl = API_BASE_URL + "chat"; 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("session_id", sessionId);
        body.add("message", message);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        Long userId = sessionMap.get(sessionId);

        int tokensNeeded = calculateTokens(message);
        if (tokensNeeded == -1) {
            return Map.of("status", "error", "message", "Failed to calculate token usage.");
        }

        if (!tokenQuotaService.hasSufficientQuota(userId, tokensNeeded)) {
            return Map.of("status", "error", "message", "Daily token quota exceeded.");
        }
        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("response")) {
            Map<String, Object> innerResponse = (Map<String, Object>) responseBody.get("response");
            Integer totalTokens = (Integer) innerResponse.get("total_tokens");
            
            tokenQuotaService.updateTokenUsage(userId, totalTokens);
        }
        return responseBody;
    }



    public String uploadNoteText(Long noteId, Long userId) {
        String fastApiUrl = API_BASE_URL + "upload-note-text";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        try {
            ProcessedNote pn = processedNoteRepository.findByNoteId(noteId);
            if (pn == null) {
                throw new IllegalArgumentException("No note found with ID: " + noteId);
            }
    
            String extractedText = pn.getText();
            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new IllegalArgumentException("Extracted text is empty for note ID: " + noteId);
            }
    
            Map<String, String> body = Map.of("text", extractedText);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
    
            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);
    
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if ("success".equalsIgnoreCase((String) responseBody.get("status"))) {
                    String sessionId = (String) responseBody.get("session_id");
                    sessionMap.put(sessionId, userId);
                    logger.info("File uploaded successfully. Session ID: {}", sessionId);
                    return sessionId;
                } else {
                    logger.error("File upload failed. Response: {}", responseBody);
                    throw new RuntimeException("File upload failed: " + responseBody);
                }
            } else {
                logger.error("Unexpected response status: {}", response.getStatusCode());
                throw new RuntimeException("Unexpected response status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error occurred while uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Error occurred while uploading file: " + e.getMessage(), e);
        }
    }
    
    

    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NoteProcessingResult {
        
        private String status;
        private String message;
        private String summary;
        private List<Map<String, Object>> quizJson;
        private String text;

        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public String getMessage() {
            return message;
        }
    
        public void setMessage(String message) {
            this.message = message;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<Map<String, Object>> getQuizJson() {
            return quizJson;
        }
    
        public void setQuizJson(List<Map<String, Object>> quizJson) {
            this.quizJson = quizJson;
        }

        public void setText(String text) {
            this.text =  text;
        }

        public String getText() {
            return text;
        }
    }
}
