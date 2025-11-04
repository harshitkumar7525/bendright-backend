package com.bendright.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final com.bendright.backend.repository.UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   com.bendright.backend.repository.UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Now the token encodes userId (subject/uid) and uname. Use uid to load user from DB.
                if (!jwtService.isTokenValid(jwt)) {
                    // Token present but invalid/expired -> return 401 immediately
                    logger.debug("JWT present but invalid or expired");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                    return;
                }

                Long userId = jwtService.extractUserId(jwt);
                if (userId != null) {
                    var optUser = userRepository.findById(userId);
                    if (optUser.isPresent()) {
                        var user = optUser.get();
                        UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities("ROLE_USER")
                                .build();
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        logger.debug("User id from JWT not found in DB: {}", userId);
                    }
                }
            } catch (Exception ex) {
                logger.debug("Failed to authenticate JWT: {}", ex.getMessage());
                // If anything goes wrong while parsing the token, return 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                try {
                    response.getWriter().write("{\"error\":\"Failed to parse token\"}");
                } catch (IOException ioe) {
                    logger.debug("Failed to write unauthorized response: {}", ioe.getMessage());
                }
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
