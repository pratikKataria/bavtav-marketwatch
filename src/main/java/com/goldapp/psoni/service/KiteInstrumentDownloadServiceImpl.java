package com.goldapp.psoni.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KiteInstrumentDownloadServiceImpl implements KiteInstrumentDownloadService {

    private final RestTemplate restTemplate;

    @Value("${kite.instruments.url}")
    private String instrumentsUrl;


    @Override
    public byte[] downloadInstrumentFile() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Kite-Version", "3");

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                instrumentsUrl,
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to download Kite instrument file");
        }

        return response.getBody();
    }

    @Override
    public String getFileName() {
        return "kite_instruments.csv";
    }
}