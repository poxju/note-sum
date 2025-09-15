package com.poxju.proksi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.poxju.proksi.model.User;
import com.poxju.proksi.api.request.AuthenticationRequest;
import com.poxju.proksi.api.request.RegisterRequest;
import com.poxju.proksi.api.response.AuthenticationResponse;
import com.poxju.proksi.model.Role;
import com.poxju.proksi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationConfiguration authenticationConfiguration;

    public void authenticate(String email, String password) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
    }
    public AuthenticationResponse register(RegisterRequest request){
        var user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.AGENT)
            .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build(); 
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        var user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found IN SERVICE"));
            
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build(); 
    }

}
