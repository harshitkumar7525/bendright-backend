package com.bendright.backend.service;

import com.bendright.backend.model.Session;
import com.bendright.backend.model.SessionStatus;
import com.bendright.backend.model.User;
import com.bendright.backend.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(User user, String statusStr, String dateStr, String asana) {
        SessionStatus status = SessionStatus.valueOf(statusStr.toUpperCase());
        LocalDate date = LocalDate.parse(dateStr);
        Session s = new Session(status, date, asana, user);
        return sessionRepository.save(s);
    }
}
