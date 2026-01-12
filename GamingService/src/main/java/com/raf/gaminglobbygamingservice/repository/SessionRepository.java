package com.raf.gaminglobbygamingservice.repository;

import com.raf.gaminglobbygamingservice.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long>,
        JpaSpecificationExecutor<Session> {

    List<Session> findByOrganizer(Long organizerId);
}
