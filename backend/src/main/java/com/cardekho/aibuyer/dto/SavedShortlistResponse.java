package com.cardekho.aibuyer.dto;

import java.time.Instant;
import java.util.List;

public record SavedShortlistResponse(
        Long id,
        String name,
        String buyerSummary,
        List<CarDto> cars,
        Instant createdAt
) {
}
