package com.cardekho.aibuyer.dto;

import jakarta.validation.constraints.NotBlank;

public record BuyerIntentRequest(
        @NotBlank String prompt
) {
}
