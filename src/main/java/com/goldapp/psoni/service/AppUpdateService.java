package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.AppUpdateRequest;
import com.goldapp.psoni.dto.AppUpdateResponse;
import com.goldapp.psoni.entity.AppUpdate;
import com.goldapp.psoni.mapper.AppUpdateMapper;
import com.goldapp.psoni.repository.AppUpdateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUpdateService {

    private final AppUpdateRepository repo;

    @Transactional
    public AppUpdateResponse create(AppUpdateRequest req) {
        AppUpdate saved = repo.save(AppUpdateMapper.toEntity(req));
        return AppUpdateMapper.toDto(saved);
    }

    @Transactional
    public AppUpdateResponse update(Long id, AppUpdateRequest req) {
        return repo.findById(id)
                .map(e -> { AppUpdateMapper.updateEntity(e, req); return AppUpdateMapper.toDto(e); })
                .orElseThrow(() -> new IllegalArgumentException("AppUpdate " + id + " not found"));
    }

    @Transactional()
    public AppUpdateResponse latestForPlatform(String platform) {
        return repo.findFirstByPlatformOrderByVersionDesc(platform)
                .map(AppUpdateMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("No update data for " + platform));
    }
}