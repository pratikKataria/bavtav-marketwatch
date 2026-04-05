package com.goldapp.psoni.dto;

public record AppUpdateResponse(
        Long      id,
        String    platform,
        String    version,
        boolean   mandatory,
        String    releaseNotes,
        String    downloadUrl,
        String    createdAt,
        String    updatedAt
) { }