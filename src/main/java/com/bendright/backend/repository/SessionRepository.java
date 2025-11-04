package com.bendright.backend.repository;

import com.bendright.backend.model.Session;
import com.bendright.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
	List<Session> findByUser(User user);
}
