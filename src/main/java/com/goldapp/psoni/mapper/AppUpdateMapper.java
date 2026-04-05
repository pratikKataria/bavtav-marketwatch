package com.goldapp.psoni.mapper;

import com.goldapp.psoni.dto.AppUpdateRequest;
import com.goldapp.psoni.dto.AppUpdateResponse;
import com.goldapp.psoni.entity.AppUpdate;

import java.time.format.DateTimeFormatter;

/** Hand-written mapper – no external library required. */
public final class AppUpdateMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private AppUpdateMapper() { }   // utility class

    /* ---------- entity ⇆ DTO ---------- */

    public static AppUpdate toEntity(AppUpdateRequest dto) {
        if (dto == null) return null;

        AppUpdate e = new AppUpdate();
        e.setPlatform(dto.platform().toUpperCase());
        e.setVersion(dto.version());
        e.setMandatory(Boolean.TRUE.equals(dto.mandatory()));
        e.setReleaseNotes(dto.releaseNotes());
        e.setDownloadUrl(dto.downloadUrl());
        return e;
    }

    public static AppUpdateResponse toDto(AppUpdate e) {
        if (e == null) return null;

        return new AppUpdateResponse(
                e.getId(),
                e.getPlatform(),
                e.getVersion(),
                e.isMandatory(),
                e.getReleaseNotes(),
                e.getDownloadUrl(),
                e.getCreatedAt() == null ? null : ISO.format(e.getCreatedAt()),
                e.getUpdatedAt() == null ? null : ISO.format(e.getUpdatedAt())
        );
    }

    /** In-place patch for PUT/PATCH calls (nulls are ignored). */
    public static void updateEntity(AppUpdate e, AppUpdateRequest dto) {
        if (e == null || dto == null) return;

        if (dto.platform()     != null) e.setPlatform(dto.platform().toUpperCase());
        if (dto.version()      != null) e.setVersion(dto.version());
        if (dto.mandatory()    != null) e.setMandatory(dto.mandatory());
        if (dto.releaseNotes() != null) e.setReleaseNotes(dto.releaseNotes());
        if (dto.downloadUrl()  != null) e.setDownloadUrl(dto.downloadUrl());
    }
}