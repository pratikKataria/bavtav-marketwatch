package com.goldapp.psoni.dto;

import com.goldapp.psoni.enums.SyncStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstrumentSyncResponseDto {

    private Long syncLogId;
    private SyncStatus status;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private Integer retryCount;
    private String message;
}