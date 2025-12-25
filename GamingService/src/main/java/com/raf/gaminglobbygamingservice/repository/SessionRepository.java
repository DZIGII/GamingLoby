package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
