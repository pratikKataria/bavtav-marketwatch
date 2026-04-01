package com.goldapp.psoni.service;

import com.goldapp.psoni.dto.InstrumentSyncResponseDto;
import com.goldapp.psoni.dto.KiteInstrumentCsvDto;
import com.goldapp.psoni.entity.InstrumentSyncErrorLog;
import com.goldapp.psoni.entity.InstrumentSyncLog;
import com.goldapp.psoni.enums.SyncStatus;
import com.goldapp.psoni.schedular.instrumentimport.KiteInstrumentParser;
import com.goldapp.psoni.repository.InstrumentBatchRepository;
import com.goldapp.psoni.repository.InstrumentSyncErrorLogRepository;
import com.goldapp.psoni.repository.InstrumentSyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class KiteInstrumentSyncServiceImpl implements KiteInstrumentSyncService {

    private final KiteInstrumentDownloadService downloadService;
    private final KiteInstrumentParser parser;
    private final InstrumentBatchRepository batchRepository;
    private final InstrumentSyncLogRepository syncLogRepository;
    private final InstrumentSyncErrorLogRepository errorLogRepository;
    private final EmailService emailService;

    @Value("${instrument.sync.batch-size:500}")
    private int batchSize;

    @Value("${instrument.sync.max-retry:3}")
    private int maxRetry;

    @Override
    public InstrumentSyncResponseDto syncInstruments() {
        InstrumentSyncLog syncLog = createSyncLog();

        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetry) {
            attempt++;
            try {
                updateRetryCount(syncLog, attempt - 1);

                SyncExecutionResult result = processSync(syncLog);

                syncLog.setStatus(result.failedRecords > 0 ? SyncStatus.PARTIAL_SUCCESS : SyncStatus.SUCCESS);
                syncLog.setEndTime(LocalDateTime.now());
                syncLog.setTotalRecords(result.totalRecords);
                syncLog.setProcessedRecords(result.processedRecords);
                syncLog.setSuccessRecords(result.successRecords);
                syncLog.setFailedRecords(result.failedRecords);
                syncLog.setRemarks("Instrument sync completed successfully");
                syncLogRepository.save(syncLog);

                return InstrumentSyncResponseDto.builder()
                        .syncLogId(syncLog.getId())
                        .status(syncLog.getStatus())
                        .totalRecords(syncLog.getTotalRecords())
                        .processedRecords(syncLog.getProcessedRecords())
                        .successRecords(syncLog.getSuccessRecords())
                        .failedRecords(syncLog.getFailedRecords())
                        .retryCount(syncLog.getRetryCount())
                        .message("Instrument sync completed successfully")
                        .build();

            } catch (Exception ex) {
                lastException = ex;
                log.error("Instrument sync failed on attempt {}", attempt, ex);

                saveErrorLog(syncLog.getId(), attempt, ex);
                emailService.sendInstrumentSyncFailureEmail(syncLog.getId(), attempt, ex.getMessage());

                if (attempt >= maxRetry) {
                    syncLog.setStatus(SyncStatus.FAILED);
                    syncLog.setEndTime(LocalDateTime.now());
                    syncLog.setRemarks("Instrument sync failed after max retries: " + ex.getMessage());
                    syncLogRepository.save(syncLog);
                }
            }
        }

        return InstrumentSyncResponseDto.builder()
                .syncLogId(syncLog.getId())
                .status(SyncStatus.FAILED)
                .totalRecords(syncLog.getTotalRecords())
                .processedRecords(syncLog.getProcessedRecords())
                .successRecords(syncLog.getSuccessRecords())
                .failedRecords(syncLog.getFailedRecords())
                .retryCount(maxRetry)
                .message(lastException != null ? lastException.getMessage() : "Instrument sync failed")
                .build();
    }

    protected SyncExecutionResult processSync(InstrumentSyncLog syncLog) {

        byte[] csvBytes = downloadService.downloadInstrumentFile();
        String fileName = downloadService.getFileName();

        syncLog.setFileName(fileName);
        syncLogRepository.save(syncLog);

        List<KiteInstrumentCsvDto> records = parser.parse(csvBytes);

        if (records.isEmpty()) {
            throw new RuntimeException("No instrument records found in Kite file");
        }

        int totalRecords = records.size();
        int processedRecords = 0;
        int successRecords = 0;
        int failedRecords = 0;

        Set<String> currentKeys = new HashSet<>(totalRecords);

        int batchCounter = 0;

        for (int start = 0; start < totalRecords; start += batchSize) {

            int end = Math.min(start + batchSize, totalRecords);
            List<KiteInstrumentCsvDto> batch = records.subList(start, end);

            batch.forEach(dto ->
                    currentKeys.add(dto.getExchange() + "||" + dto.getSymbol())
            );

            try {

                int[] result = batchRepository.batchUpsert(batch);

                processedRecords += batch.size();
                successRecords += result.length;

            } catch (Exception ex) {

                failedRecords += batch.size();
                processedRecords += batch.size();

                log.error("Batch failed from index {} to {}", start, end, ex);

                throw new RuntimeException(
                        "Batch processing failed for records " + start + " to " + (end - 1),
                        ex
                );
            }

            batchCounter++;

            if (batchCounter % 5 == 0) {
                syncLog.setTotalRecords(totalRecords);
                syncLog.setProcessedRecords(processedRecords);
                syncLog.setSuccessRecords(successRecords);
                syncLog.setFailedRecords(failedRecords);

                syncLogRepository.save(syncLog);
            }
        }

        batchRepository.markMissingInstrumentsInactive(new ArrayList<>(currentKeys));

        syncLog.setTotalRecords(totalRecords);
        syncLog.setProcessedRecords(processedRecords);
        syncLog.setSuccessRecords(successRecords);
        syncLog.setFailedRecords(failedRecords);

        syncLogRepository.save(syncLog);

        return new SyncExecutionResult(
                totalRecords,
                processedRecords,
                successRecords,
                failedRecords
        );
    }
    private InstrumentSyncLog createSyncLog() {
        InstrumentSyncLog logEntity = InstrumentSyncLog.builder()
                .jobName("KITE_INSTRUMENT_SYNC")
                .status(SyncStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .retryCount(0)
                .totalRecords(0)
                .processedRecords(0)
                .successRecords(0)
                .failedRecords(0)
                .remarks("Instrument sync initiated")
                .build();

        return syncLogRepository.save(logEntity);
    }

    private void updateRetryCount(InstrumentSyncLog syncLog, int retryCount) {
        syncLog.setRetryCount(retryCount);
        syncLogRepository.save(syncLog);
    }

    private void saveErrorLog(Long syncLogId, int attemptNo, Exception ex) {
        InstrumentSyncErrorLog errorLog = InstrumentSyncErrorLog.builder()
                .syncLogId(syncLogId)
                .attemptNo(attemptNo)
                .errorMessage(ex.getMessage())
                .stackTrace(getStackTrace(ex))
                .build();

        errorLogRepository.save(errorLog);
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private record SyncExecutionResult(
            int totalRecords,
            int processedRecords,
            int successRecords,
            int failedRecords
    ) {}
}