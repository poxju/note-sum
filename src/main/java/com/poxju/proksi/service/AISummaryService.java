package com.poxju.proksi.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poxju.proksi.model.Note;
import com.poxju.proksi.repository.NoteRepository;

import lombok.RequiredArgsConstructor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class AISummaryService {

    private static final Logger logger = LoggerFactory.getLogger(AISummaryService.class);
    private static final long ASYNC_TIMEOUT_SECONDS = 120; // 2 minutes timeout for AI processing
    
    private final NoteRepository noteRepository;
    private final HuggingFaceService huggingFaceService;

    @Async("taskExecutor")
    public CompletableFuture<Void> generateSummaryAsync(Long noteId) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Single database query - load note once
                final Note note = noteRepository.findById(noteId).orElse(null);
                if (note == null) {
                    logger.warn("Note not found: {}", noteId);
                    return;
                }

                // Update status to processing
                note.setStatus("processing");
                noteRepository.save(note);
                logger.info("Started AI processing for note: {}", noteId);

                // Store note content in final variable for lambda usage
                final String noteContent = note.getContent();

                // Generate summary with timeout protection
                String summary = CompletableFuture
                    .supplyAsync(() -> huggingFaceService.summarizeText(noteContent))
                    .get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                // Update note with summary and status in single save
                note.setSummary(summary);
                note.setStatus("done");
                noteRepository.save(note);
                
                logger.info("AI processing completed for note: {}", noteId);

            } catch (TimeoutException e) {
                logger.error("Timeout processing summary for note {} after {} seconds", noteId, ASYNC_TIMEOUT_SECONDS);
                updateNoteStatus(null, noteId, "Timeout: Processing took too long");
            } catch (Exception e) {
                logger.error("Error processing summary for note {}", noteId, e);
                updateNoteStatus(null, noteId, e.getMessage());
            }
        });
    }
    
    private void updateNoteStatus(Note note, Long noteId, String errorMessage) {
        try {
            if (note == null) {
                note = noteRepository.findById(noteId).orElse(null);
            }
            if (note != null) {
                note.setStatus("failed");
                // Truncate error message to prevent database issues with very long messages
                String errorMsg = errorMessage;
                if (errorMsg != null && errorMsg.length() > 500) {
                    errorMsg = errorMsg.substring(0, 497) + "...";
                }
                note.setSummary("Summary generation failed: " + errorMsg);
                noteRepository.save(note);
            }
        } catch (Exception ex) {
            logger.error("Error updating failed status for note {}", noteId, ex);
        }
    }
}