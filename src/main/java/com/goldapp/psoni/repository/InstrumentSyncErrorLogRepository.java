package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.InstrumentSyncErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentSyncErrorLogRepository extends JpaRepository<InstrumentSyncErrorLog, Long> {
}