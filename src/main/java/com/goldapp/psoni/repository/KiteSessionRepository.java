package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.KiteSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface KiteSessionRepository extends JpaRepository<KiteSession, Long> {

    /** Fetch today's active session token */
    Optional<KiteSession> findBySessionDateAndActiveTrue(LocalDate date);

    /** Deactivate all previous sessions before inserting a fresh one */
    @Modifying
    @Query("UPDATE KiteSession s SET s.active = false WHERE s.active = true")
    void deactivateAllSessions();
}