package com.bendright.backend.controller;

import com.bendright.backend.dto.SessionRequest;
import com.bendright.backend.model.User;
import com.bendright.backend.repository.UserRepository;
import com.bendright.backend.service.SessionService;
import com.bendright.backend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public SessionController(SessionService sessionService, UserRepository userRepository, JwtService jwtService) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/{uid}/sessions/{pose}")
    public ResponseEntity<?> createSession(@PathVariable Long uid,
            @PathVariable String pose,
            @RequestBody SessionRequest req) {
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("User not found"));
        var s = sessionService.createSession(user, req.status(), req.date(), pose);
        return ResponseEntity.ok(s);
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> listSessionsForCurrentUser(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Missing or invalid Authorization header"));
        }
        String jwt = authHeader.substring(7);
        Long uid = jwtService.extractUserId(jwt);
        if (uid == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid token"));
        }
        User user = userRepository.findById(uid).orElseThrow(() -> new RuntimeException("User not found"));
        List<?> sessions = sessionService.listSessionsForUser(user);
        return ResponseEntity.ok(sessions);
    }
}
