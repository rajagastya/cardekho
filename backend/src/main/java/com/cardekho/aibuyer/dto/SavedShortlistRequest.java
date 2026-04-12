package com.cardekho.aibuyer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SavedShortlistRequest(
        @NotBlank String name,
        @NotBlank String buyerSummary,
        @NotEmpty List<CarDto> cars
) {
}
