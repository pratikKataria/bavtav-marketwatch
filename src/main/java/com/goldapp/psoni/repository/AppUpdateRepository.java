package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.AppUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUpdateRepository extends JpaRepository<AppUpdate, Long> {
    Optional<AppUpdate> findFirstByPlatformOrderByVersionDesc(String platform);
}