package com.cardekho.aibuyer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BuyerPreferencesRequest(
        @NotBlank String name,
        @NotNull @Min(4) @Max(50) Integer budgetLakh,
        @NotBlank String primaryUse,
        @NotBlank String preferredTransmission,
        @NotBlank String fuelPreference,
        @NotBlank String bodyStyle,
        @NotNull @Min(1) @Max(8) Integer familySize,
        @NotBlank String priority,
        @NotBlank String drivingMix,
        String notes,
        List<String> mustHaveFeatures
) {
}
