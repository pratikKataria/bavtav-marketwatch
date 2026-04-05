package com.goldapp.psoni.controller;

import com.goldapp.psoni.dto.AppUpdateRequest;
import com.goldapp.psoni.dto.AppUpdateResponse;
import com.goldapp.psoni.service.AppUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/app-updates")
@RequiredArgsConstructor
public class AppUpdateController {

    private final AppUpdateService service;

    /* POST  /api/app-updates               – create new version entry */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppUpdateResponse create(@Valid @RequestBody AppUpdateRequest req) {
        return service.create(req);
    }

    /* PUT   /api/app-updates/{id}          – full/partial update */
    @PutMapping("/{id}")
    public AppUpdateResponse update(@PathVariable Long id,
                                    @Valid @RequestBody AppUpdateRequest req) {
        return service.update(id, req);
    }

    /* GET   /api/app-updates/latest?platform=ANDROID  – fetch latest version info */
    @GetMapping("/latest")
    public AppUpdateResponse latest(@RequestParam String platform) {
        return service.latestForPlatform(platform.toUpperCase());
    }
}