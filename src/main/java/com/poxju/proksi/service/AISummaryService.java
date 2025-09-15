package com.poxju.proksi.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.poxju.proksi.model.Note;
import com.poxju.proksi.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AISummaryService {

    private final NoteRepository noteRepository;
    private final HuggingFaceService huggingFaceService;

    @Async
    public void generateSummaryAsync(Long noteId) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note == null) {
                System.err.println("Note not found: " + noteId);
                return;
            }

            note.setStatus("processing");
            noteRepository.save(note);
            System.out.println("Started AI processing for note: " + noteId);

            String summary = huggingFaceService.summarizeText(note.getContent());
            
            note.setSummary(summary);
            note.setStatus("done");
            noteRepository.save(note);
            
            System.out.println("AI processing completed for note: " + noteId);

        } catch (Exception e) {
            System.err.println("Error processing summary for note " + noteId + ": " + e.getMessage());
            
            try {
                Note note = noteRepository.findById(noteId).orElse(null);
                if (note != null) {
                    note.setStatus("failed");
                    note.setSummary("Summary generation failed: " + e.getMessage());
                    noteRepository.save(note);
                }
            } catch (Exception ex) {
                System.err.println("Error updating failed status: " + ex.getMessage());
            }
        }
    }
}