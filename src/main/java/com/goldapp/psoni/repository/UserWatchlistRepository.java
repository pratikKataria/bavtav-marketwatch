package com.goldapp.psoni.repository;

import com.goldapp.psoni.entity.UserWatchlist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Long> {

    List<UserWatchlist> findByUserIdOrderByDisplayOrderAsc(Long userId);

    Optional<UserWatchlist> findByUserIdAndInstrumentId(Long userId, Long instrumentId);

    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByUserIdAndInstrumentId(Long userId, Long instrumentId);

    @Query("""
            SELECT uw, im
            FROM UserWatchlist uw
            JOIN InstrumentMaster im
              ON uw.exchange = im.exchange AND uw.symbol = im.symbol
            WHERE uw.userId = :userId
              AND im.active = true
            ORDER BY uw.displayOrder ASC
            """)
    List<Object[]> findWatchlistWithInstrumentByUserId(@Param("userId") Long userId);
}