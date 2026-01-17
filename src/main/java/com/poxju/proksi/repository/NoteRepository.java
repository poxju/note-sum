package com.poxju.proksi.repository;

import com.poxju.proksi.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);
    Page<Note> findByUserId(Long userId, Pageable pageable);
}
