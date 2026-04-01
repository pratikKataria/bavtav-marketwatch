package com.goldapp.psoni.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "instrument_master",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_instrument_master_exchange_symbol", columnNames = {"exchange", "symbol"})
        },
        indexes = {
                @Index(name = "idx_instrument_exchange", columnList = "exchange"),
                @Index(name = "idx_instrument_symbol", columnList = "symbol"),
                @Index(name = "idx_instrument_name", columnList = "name")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exchange", nullable = false, length = 20)
    private String exchange;

    @Column(name = "symbol", nullable = false, length = 100)
    private String symbol;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "instrument_token")
    private Long instrumentToken;

    @Column(name = "exchange_token")
    private Long exchangeToken;

    @Column(name = "instrument_type", length = 50)
    private String instrumentType;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "strike_price", precision = 18, scale = 4)
    private BigDecimal strikePrice;

    @Column(name = "tick_size", precision = 18, scale = 4)
    private BigDecimal tickSize;

    @Column(name = "lot_size")
    private Integer lotSize;

    @Column(name = "segment", length = 50)
    private String segment;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn;

    @Column(name = "default_symbol")
    private Boolean defaultSymbol;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdOn == null) {
            createdOn = now;
        }
        if (updatedOn == null) {
            updatedOn = now;
        }
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedOn = LocalDateTime.now();
    }
}