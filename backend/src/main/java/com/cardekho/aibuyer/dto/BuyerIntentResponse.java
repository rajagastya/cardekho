package com.cardekho.aibuyer.dto;

import java.util.List;

public record BuyerIntentResponse(
        BuyerPreferencesRequest preferences,
        List<String> signals,
        String source
) {
}
