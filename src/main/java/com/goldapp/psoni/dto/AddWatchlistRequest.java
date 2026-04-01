package com.goldapp.psoni.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWatchlistRequest {

    @NotNull
    private Long instrumentId;

}