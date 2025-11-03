package com.bendright.backend.controller;

import com.bendright.backend.dto.SessionRequest;
import com.bendright.backend.model.User;
import com.bendright.backend.repository.UserRepository;
import com.bendright.backend.security.JwtService;
import com.bendright.backend.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody SessionRequest req, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        var s = sessionService.createSession(user, req.status(), req.date(), req.asana());
        return ResponseEntity.ok(s);
    }
}
