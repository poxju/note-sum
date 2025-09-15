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

    @Async
    public void generateSummaryAsync(Long noteId) {
        try {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note == null) {
                return;
            }

            note.setStatus("processing");
            noteRepository.save(note);

            String summary = simulateAISummary(note.getContent());

            Thread.sleep(3000); // 3 saniye

            note.setSummary(summary);
            note.setStatus("done");
            noteRepository.save(note);

        } catch (Exception e) {
            Note note = noteRepository.findById(noteId).orElse(null);
            if (note != null) {
                note.setStatus("failed");
                noteRepository.save(note);
            }
            e.printStackTrace();
        }
    }

    private String simulateAISummary(String content) {
        if (content.length() < 50) {
            return "Short note: " + content.substring(0, Math.min(content.length(), 30)) + "...";
        } else if (content.length() < 200) {
            return "Medium-length note covering key points from the original content. " +
                   "Main topic appears to be about: " + extractKeywords(content);
        } else {
            return "Comprehensive note with detailed information. " +
                   "Key themes include: " + extractKeywords(content) + 
                   ". This summary captures the essential points from a longer text.";
        }
    }

    private String extractKeywords(String content) {
        String[] words = content.toLowerCase().split("\\s+");
        if (words.length > 0) {
            return words[0] + (words.length > 1 ? ", " + words[1] : "");
        }
        return "general topics";
    }
}