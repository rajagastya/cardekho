package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.BuyerPreferencesRequest;
import com.cardekho.aibuyer.dto.RecommendationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationServiceTest {

    @Test
    void returnsFiveRecommendationsForSafetyFocusedFamilyBuyer() {
        ObjectMapper objectMapper = new ObjectMapper();
        CatalogService catalogService = new CatalogService(objectMapper);
        catalogService.loadCars();
        GeminiSummaryService geminiSummaryService = new GeminiSummaryService(objectMapper);
        RecommendationService recommendationService = new RecommendationService(catalogService, geminiSummaryService);

        BuyerPreferencesRequest request = new BuyerPreferencesRequest(
                "Rahul",
                16,
                "mixed",
                "Automatic",
                "Petrol",
                "SUV",
                4,
                "Safety",
                "City",
                "Rear seat comfort matters",
                List.of("Automatic")
        );

        RecommendationResponse response = recommendationService.recommend(request);

        assertEquals(5, response.recommendations().size());
        assertTrue(response.recommendations().get(0).car().safetyRating() >= 4);
        assertTrue(response.recommendations().get(0).confidenceScore() >= 70);
        assertTrue(response.recommendations().get(0).whyThisCarTip() != null && !response.recommendations().get(0).whyThisCarTip().isBlank());
        assertTrue(response.aiSummary() != null && !response.aiSummary().isBlank());
    }
}
