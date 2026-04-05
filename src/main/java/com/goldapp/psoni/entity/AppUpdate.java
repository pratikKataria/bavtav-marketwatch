package com.goldapp.psoni.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity @Table(name = "app_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUpdate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String platform;            // ANDROID / IOS

    @Column(nullable = false, length = 20)
    private String version;             // e.g. 2.3.1

    @Column(nullable = false)
    private boolean mandatory;          // force-update?

    @Lob
    private String releaseNotes;

    @Column(nullable = false)
    private String downloadUrl;

    @CreationTimestamp private Instant createdAt;
    @UpdateTimestamp   private Instant updatedAt;
}