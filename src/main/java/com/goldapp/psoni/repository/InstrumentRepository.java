package com.goldapp.psoni.repository;

import com.goldapp.psoni.dto.InstrumentSearchDto;
import com.goldapp.psoni.entity.InstrumentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstrumentRepository extends JpaRepository<InstrumentMaster, Long> {

    List<InstrumentMaster> findByDefaultSymbolTrue();

    InstrumentMaster findByInstrumentToken(Long token);

    @Query(value = """
                SELECT
                    im.id              AS instrument_id,
                    im.symbol,
                    im.exchange,
                    im.name,
                    (uw.id IS NOT NULL) AS subscribed
                FROM instrument_master       AS im
                LEFT JOIN user_watchlist     AS uw
                       ON uw.instrumentid = im.id         -- same instrument
                      AND uw.userid      = :userId        -- for *this* user only
                WHERE
                      (COALESCE(:exchange, '') = '' OR im.exchange = :exchange)
                  AND im.active                          -- boolean column
                  AND (im.symbol ILIKE '%' || :query || '%'
                       OR im.name   ILIKE '%' || :query || '%')
                ORDER BY im.symbol
                LIMIT 20;
            """, nativeQuery = true)
    List<InstrumentSearchDto> searchInstruments(
            @Param("exchange") String exchange,
            @Param("query") String query,
            @Param("userId") Long userId
    );
}