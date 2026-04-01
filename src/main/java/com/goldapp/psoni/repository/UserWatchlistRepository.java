package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.UserWatchlist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Long> {

    List<UserWatchlist> findByUserIdOrderByDisplayOrderAsc(Long userId);

    Optional<UserWatchlist> findByUserIdAndInstrumentId(Long userId, Long instrumentId);

    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByUserIdAndInstrumentId(Long userId, Long instrumentId);
}