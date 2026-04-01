package com.goldapp.psoni.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "instrument_sync_error_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentSyncErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long syncLogId;

    @Column(nullable = false)
    private Integer attemptNo;

    @Column(length = 5000)
    private String errorMessage;

    @Lob
    private String stackTrace;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @PrePersist
    public void prePersist() {
        if (createdOn == null) {
            createdOn = LocalDateTime.now();
        }
    }
}