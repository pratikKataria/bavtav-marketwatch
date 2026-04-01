package com.goldapp.psoni.entity;

import com.goldapp.psoni.enums.SyncStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_sync_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer successRecords;

    private Integer failedRecords;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(length = 255)
    private String fileName;

    @Column(length = 2000)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdOn == null) {
            createdOn = now;
        }
        if (updatedOn == null) {
            updatedOn = now;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
    }
}