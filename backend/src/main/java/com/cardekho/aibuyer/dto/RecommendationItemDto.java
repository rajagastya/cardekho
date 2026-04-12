package com.cardekho.aibuyer.dto;

import java.util.List;

public record RecommendationItemDto(
        CarDto car,
        int score,
        int confidenceScore,
        List<String> reasons,
        String caution,
        String whyThisCarTip
) {
}
