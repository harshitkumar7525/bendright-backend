package com.bendright.backend.controller;

import com.bendright.backend.dto.LoginRequest;
import com.bendright.backend.dto.SignupRequest;
import com.bendright.backend.model.User;
import com.bendright.backend.repository.UserRepository;
import com.bendright.backend.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // ✅ SIGNUP endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        // Case-insensitive email check (requires repo method: existsByEmailIgnoreCase)
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Email already registered"
                    ));
        }

        // Save new user
        User user = new User(
                req.userName(),
                req.email(),
                passwordEncoder.encode(req.password())
        );
        userRepository.save(user);

        // Generate JWT for instant login (encode userId and userName)
        String token = jwtService.generateToken(user.getId(), user.getUserName());

        // Return a proper JSON response with status 201
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(Map.of(
                        "success", true,
                        "message", "User registered successfully",
                        "userId", user.getId(),
                        "email", user.getEmail(),
                        "userName", user.getUserName(),
                        "token", token
                ));
    }

    // ✅ LOGIN endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            // Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );

            // Find user entity and generate token after successful authentication
            User user = userRepository.findByEmailIgnoreCase(req.email())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String token = jwtService.generateToken(user.getId(), user.getUserName());

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Login successful",
                    "token", token,
                    "userId", user.getId(),
                    "userName", user.getUserName(),
                    "email", user.getEmail()
            ));
        } catch (Exception e) {
            // Authentication failed
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid email or password"
                    ));
        }
    }
}
