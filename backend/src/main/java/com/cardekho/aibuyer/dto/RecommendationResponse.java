package com.cardekho.aibuyer.dto;

import java.util.List;

public record RecommendationResponse(
        String buyerSummary,
        String aiSummary,
        List<RecommendationItemDto> recommendations,
        List<String> tradeOffs
) {
}
