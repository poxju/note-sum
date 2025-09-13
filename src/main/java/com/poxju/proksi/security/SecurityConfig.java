package com.poxju.proksi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import com.poxju.proksi.service.UserService;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor 
@EnableWebSecurity

public class SecurityConfig {
    @Autowired
    private final UserService appUserService;
     
    @Bean
    public UserDetailsService userDetailsService() {
        return appUserService;
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); 
        provider.setUserDetailsService(appUserService);
        return provider;
    }
     
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSec) throws Exception {
        return httpSec
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(httpForm -> {
                httpForm
                    .loginPage("/login")
                    .permitAll();
            })

            .authorizeHttpRequests( registry -> {
                registry
                    .requestMatchers("/req/signup").permitAll()
                    .anyRequest().authenticated();
            })

            .build();

    } 
}
