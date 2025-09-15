package com.poxju.proksi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.poxju.proksi.api.request.RegisterRequest;
import com.poxju.proksi.service.AuthenticationService;
import com.poxju.proksi.service.AISummaryService;
import com.poxju.proksi.model.Note;
import com.poxju.proksi.model.User;
import com.poxju.proksi.repository.NoteRepository;
import com.poxju.proksi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentController {

    private final AuthenticationService authenticationService;
    private final AISummaryService aiSummaryService;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Home page accessed by: " + (userDetails != null ? userDetails.getUsername() : "anonymous"));
        
        if (userDetails != null) {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            System.out.println("Current user found: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            if (currentUser != null) {
                var notes = noteRepository.findByUserId(currentUser.getId());
                System.out.println("Found " + notes.size() + " notes for user");
                model.addAttribute("notes", notes);
            }
        }
        return "home";
    }

    @PostMapping("/notes")
    public String createNote(String title, String content, @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("createNote called with title: " + title + ", content: " + content);
        System.out.println("UserDetails: " + (userDetails != null ? userDetails.getUsername() : "null"));
        
        if (userDetails != null) {
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            System.out.println("Current user found: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            if (currentUser != null) {
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);
                note.setUser(currentUser);
                note.setStatus("queued"); // AI özet işlemi için sıraya al
                
                System.out.println("Saving note: " + note.getTitle());
                Note savedNote = noteRepository.save(note);
                System.out.println("Note saved with ID: " + savedNote.getId());
                
                // AI özet işlemini başlat (async)
                aiSummaryService.generateSummaryAsync(savedNote.getId());
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
            // Ideally, handle specific exceptions, e.g., user already exists
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again.");
            return "redirect:/signup";
        }
    }
}
