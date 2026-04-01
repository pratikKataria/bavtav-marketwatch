package com.goldapp.psoni.service;

public interface KiteInstrumentDownloadService {
    byte[] downloadInstrumentFile();
    String getFileName();
}