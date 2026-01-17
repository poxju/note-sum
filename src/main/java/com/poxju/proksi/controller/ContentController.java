package com.poxju.proksi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.poxju.proksi.api.request.RegisterRequest;
import com.poxju.proksi.service.AuthenticationService;
import com.poxju.proksi.service.AISummaryService;
import com.poxju.proksi.model.Note;
import com.poxju.proksi.model.Role;
import com.poxju.proksi.model.User;
import com.poxju.proksi.repository.NoteRepository;
import com.poxju.proksi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
public class ContentController {

    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    private final AuthenticationService authenticationService;
    private final AISummaryService aiSummaryService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(
            Model model, 
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (userDetails != null) {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            
            if (currentUser != null) {
                model.addAttribute("username", currentUser.getUsernameField()); 
                Pageable pageable = PageRequest.of(page, size);
                
                if (currentUser.getRole() == Role.ADMIN) {
                    Page<Note> notesPage = noteRepository.findAll(pageable);
                    Page<User> usersPage = userRepository.findAll(pageable);
                    
                    model.addAttribute("notes", notesPage.getContent());
                    model.addAttribute("users", usersPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", notesPage.getTotalPages());
                    model.addAttribute("totalNotes", notesPage.getTotalElements());
                    model.addAttribute("isAdmin", true);
                    return "admin-home";
                } else {
                    Page<Note> notesPage = noteRepository.findByUserId(currentUser.getId(), pageable);
                    
                    model.addAttribute("notes", notesPage.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", notesPage.getTotalPages());
                    model.addAttribute("totalNotes", notesPage.getTotalElements());
                    model.addAttribute("isAdmin", false);
                    return "home";
                }
            }
        }
        return "home";
    }

    @PostMapping("/notes")
    public String createNote(String title, String content, @AuthenticationPrincipal UserDetails userDetails) {
        logger.debug("createNote called with title: {}", title);
        
        if (userDetails != null) {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            
            if (currentUser != null) {
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);
                note.setUser(currentUser);
                note.setStatus("queued"); 
                
                Note savedNote = noteRepository.save(note);
                logger.info("Note saved with ID: {} by user: {}", savedNote.getId(), currentUser.getEmail());
                
                // Fire and forget async processing - don't wait for completion
                aiSummaryService.generateSummaryAsync(savedNote.getId())
                    .exceptionally(ex -> {
                        logger.error("Async summary generation failed for note {}", savedNote.getId(), ex);
                        return null;
                    });
            }
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(RegisterRequest request, RedirectAttributes redirectAttributes) {
        try {
            authenticationService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/signup";
        }
    }
}
