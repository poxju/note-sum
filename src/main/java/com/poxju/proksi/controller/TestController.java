package com.poxju.proksi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // HTTP Get request
    // http://localhost:8080/test

    @GetMapping("/test")
    public String test() {
        return "<html><body><h1>Hello from Spring Boot!</h1></body></html>";
    }
}
