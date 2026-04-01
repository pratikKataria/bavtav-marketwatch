package com.goldapp.psoni.schedular.instrumentimport;

import com.goldapp.psoni.dto.KiteInstrumentCsvDto;
import com.goldapp.psoni.utils.InstrumentCsvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses the CSV file published by Kite and converts it into a list of {@link KiteInstrumentCsvDto} objects.
 * <p>
 * Additional validation has been added to **ignore option contracts** whose symbol ends with
 * <code>CE</code> (Call Options) or <code>PE</code> (Put Options). Only the underlying equities / futures /
 * commodity contracts that are relevant for the dashboard will now be returned.
 */
@Service
@Slf4j
public class KiteInstrumentParserImpl implements KiteInstrumentParser {

    private static final Set<String> ALLOWED_EXCHANGES = Set.of("NSE", "NFO", "MCX");

    @Override
    public List<KiteInstrumentCsvDto> parse(byte[] csvBytes) {
        List<KiteInstrumentCsvDto> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8))) {

            String line;
            boolean firstRow = true;

            while ((line = reader.readLine()) != null) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                String[] cols = splitCsv(line);

                if (cols.length < 12) {
                    log.warn("Skipping malformed row: {}", line);
                    continue;
                }

                String exchange = InstrumentCsvUtil.safeValue(cols[11]);

                if (!ALLOWED_EXCHANGES.contains(exchange)) {
                    continue;
                }

                String symbol = InstrumentCsvUtil.safeValue(cols[2]);
                // Ignore CE / PE option symbols
                if (symbol != null) {
                    String uc = symbol.toUpperCase();
                    if (uc.endsWith("CE") || uc.endsWith("PE")) {
                        continue;
                    }
                }

                // *** New validation ***
                // Ignore option contracts – their symbols always end with CE (Call) or PE (Put)
                if (symbol != null) {
                    String upperSym = symbol.toUpperCase();
                    if (upperSym.endsWith("CE") || upperSym.endsWith("PE")) {
                        continue; // skip CE/PE symbols
                    }
                }

                KiteInstrumentCsvDto dto = KiteInstrumentCsvDto.builder()
                        .instrumentToken(parseLong(cols[0]))
                        .exchangeToken(parseLong(cols[1]))
                        .symbol(InstrumentCsvUtil.safeValue(cols[2]))
                        .name(InstrumentCsvUtil.safeValue(cols[3]))
                        .lastPrice(parseBigDecimal(cols[4]))
                        .expiryDate(parseDate(cols[5]))
                        .strikePrice(parseBigDecimal(cols[6]))
                        .tickSize(parseBigDecimal(cols[7]))
                        .lotSize(parseInteger(cols[8]))
                        .instrumentType(InstrumentCsvUtil.safeValue(cols[9]))
                        .segment(InstrumentCsvUtil.safeValue(cols[10]))
                        .exchange(InstrumentCsvUtil.safeValue(cols[11]))
                        .build();

                if (dto.getExchange() != null && dto.getSymbol() != null) {
                    result.add(dto);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse Kite instrument CSV", ex);
        }

        return result;
    }

    private String[] splitCsv(String line) {
        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private Long parseLong(String value) {
        value = InstrumentCsvUtil.safeValue(value);
        return value == null ? null : Long.parseLong(value);
    }

    private Integer parseInteger(String value) {
        value = InstrumentCsvUtil.safeValue(value);
        return value == null ? null : Integer.parseInt(value);
    }

    private BigDecimal parseBigDecimal(String value) {
        value = InstrumentCsvUtil.safeValue(value);
        return value == null ? null : new BigDecimal(value);
    }

    private LocalDate parseDate(String value) {
        value = InstrumentCsvUtil.safeValue(value);
        return value == null ? null : LocalDate.parse(value);
    }
}