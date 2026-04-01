package com.goldapp.psoni.repository;

import com.goldapp.psoni.dto.KiteInstrumentCsvDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InstrumentBatchRepositoryImpl implements InstrumentBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int[] batchUpsert(List<KiteInstrumentCsvDto> records) {
        String sql = """
                MERGE INTO instrument_master AS target
                USING (
                    SELECT
                        ? AS exchange,
                        ? AS symbol,
                        ? AS name,
                        ? AS instrument_token,
                        ? AS exchange_token,
                        ? AS instrument_type,
                        CAST(? AS DATE) AS expiry_date,
                        ? AS strike_price,
                        ? AS tick_size,
                        ? AS lot_size,
                        ? AS segment,
                        ? AS active,
                      CAST(? AS TIMESTAMP) AS created_on,
                      CAST(? AS TIMESTAMP) AS updated_on,
                        ? AS default_symbol
                ) AS source
                ON target.exchange = source.exchange AND target.symbol = source.symbol
                WHEN MATCHED THEN UPDATE SET
                    name = source.name,
                    instrument_token = source.instrument_token,
                    exchange_token = source.exchange_token,
                    instrument_type = source.instrument_type,
                    expiry_date = source.expiry_date,
                    strike_price = source.strike_price,
                    tick_size = source.tick_size,
                    lot_size = source.lot_size,
                    segment = source.segment,
                    active = source.active,
                    updated_on = source.updated_on,
                    default_symbol = source.default_symbol
                WHEN NOT MATCHED THEN INSERT (
                    exchange,
                    symbol,
                    name,
                    instrument_token,
                    exchange_token,
                    instrument_type,
                    expiry_date,
                    strike_price,
                    tick_size,
                    lot_size,
                    segment,
                    active,
                    created_on,
                    updated_on,
                    default_symbol
                ) VALUES (
                    source.exchange,
                    source.symbol,
                    source.name,
                    source.instrument_token,
                    source.exchange_token,
                    source.instrument_type,
                    source.expiry_date,
                    source.strike_price,
                    source.tick_size,
                    source.lot_size,
                    source.segment,
                    source.active,
                    source.created_on,
                    source.updated_on,
                    source.default_symbol
                )
                """;

        LocalDateTime now = LocalDateTime.now();

        int[][] results = jdbcTemplate.batchUpdate(sql, records, records.size(), (PreparedStatement ps, KiteInstrumentCsvDto dto) -> {
            ps.setString(1, dto.getExchange());
            ps.setString(2, dto.getSymbol());
            ps.setString(3, dto.getName());
            ps.setObject(4, dto.getInstrumentToken());
            ps.setObject(5, dto.getExchangeToken());
            ps.setString(6, dto.getInstrumentType());
            ps.setDate(7, dto.getExpiryDate() != null ? Date.valueOf(dto.getExpiryDate()) : null);
            ps.setBigDecimal(8, dto.getStrikePrice());
            ps.setBigDecimal(9, dto.getTickSize());
            ps.setObject(10, dto.getLotSize());
            ps.setString(11, dto.getSegment());
            ps.setBoolean(12, true);
            ps.setTimestamp(13, Timestamp.valueOf(now));
            ps.setTimestamp(14, Timestamp.valueOf(now));
            ps.setBoolean(15, Boolean.FALSE);
        });

        int total = 0;
        for (int[] batch : results) {
            total += batch.length;
        }

        return new int[]{total};
    }

    @Override
    public void markMissingInstrumentsInactive(List<String> currentKeys) {
        if (currentKeys == null || currentKeys.isEmpty()) {
            return;
        }

        String inClause = currentKeys.stream()
                .map(key -> "?")
                .collect(Collectors.joining(","));

        String sql = """
                UPDATE instrument_master
                SET active = false,
                    updated_on = ?
                WHERE CONCAT(exchange, '||', symbol) NOT IN (%s)
                """.formatted(inClause);

        Object[] params = new Object[currentKeys.size() + 1];
        params[0] = Timestamp.valueOf(LocalDateTime.now());

        for (int i = 0; i < currentKeys.size(); i++) {
            params[i + 1] = currentKeys.get(i);
        }

        jdbcTemplate.update(sql, params);
    }
}