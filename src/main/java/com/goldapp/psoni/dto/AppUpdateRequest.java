package com.goldapp.psoni.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppUpdateRequest(
        @NotBlank String platform,
        @NotBlank String version,
        @NotNull  Boolean mandatory,
        String    releaseNotes,
        @NotBlank String downloadUrl
) { }
