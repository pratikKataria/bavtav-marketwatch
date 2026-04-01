package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.InstrumentSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentSyncLogRepository extends JpaRepository<InstrumentSyncLog, Long> {
}