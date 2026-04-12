package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.BuyerPreferencesRequest;
import com.cardekho.aibuyer.dto.BuyerIntentResponse;
import com.cardekho.aibuyer.dto.CarDto;
import com.cardekho.aibuyer.dto.RecommendationItemDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GeminiSummaryService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    public GeminiSummaryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String generateSummary(BuyerPreferencesRequest request,
                                  List<RecommendationItemDto> recommendations,
                                  String buyerSummary,
                                  List<String> tradeOffs) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return fallbackSummary(recommendations, tradeOffs);
        }

        try {
            String text = generateText(buildSummaryPrompt(request, recommendations, buyerSummary, tradeOffs));
            if (text != null && !text.isBlank()) {
                return text.trim();
            }
        } catch (Exception ignored) {
            return fallbackSummary(recommendations, tradeOffs);
        }

        return fallbackSummary(recommendations, tradeOffs);
    }

    public BuyerIntentResponse parseBuyerIntent(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return null;
        }

        try {
            String text = generateText(buildIntentPrompt(prompt));
            if (text != null && !text.isBlank()) {
                return objectMapper.readValue(cleanJson(text), BuyerIntentResponse.class);
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    public Map<String, String> generateWhyThisCarTips(BuyerPreferencesRequest request, List<RecommendationItemDto> recommendations) {
        Map<String, String> fallbackTips = buildFallbackWhyTips(recommendations);
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return fallbackTips;
        }

        try {
            String text = generateText(buildWhyThisCarPrompt(request, recommendations));
            if (text == null || text.isBlank()) {
                return fallbackTips;
            }

            JsonNode root = objectMapper.readTree(cleanJson(text));
            Map<String, String> tips = new LinkedHashMap<>();
            for (RecommendationItemDto recommendation : recommendations) {
                String tip = root.path(recommendation.car().id()).asText();
                tips.put(recommendation.car().id(), tip == null || tip.isBlank() ? fallbackTips.get(recommendation.car().id()) : tip);
            }
            return tips;
        } catch (Exception ignored) {
            return fallbackTips;
        }
    }

    private String buildSummaryPrompt(BuyerPreferencesRequest request,
                                      List<RecommendationItemDto> recommendations,
                                      String buyerSummary,
                                      List<String> tradeOffs) throws Exception {
        String recommendationJson = objectMapper.writeValueAsString(recommendations.stream()
                .map(item -> Map.<String, Object>of(
                        "car", item.car().make() + " " + item.car().model() + " " + item.car().variant(),
                        "score", item.score(),
                        "confidenceScore", item.confidenceScore(),
                        "safetyRating", item.car().safetyRating(),
                        "userRating", item.car().userRating(),
                        "bootSpaceLiters", item.car().bootSpaceLiters(),
                        "reasons", item.reasons(),
                        "caution", item.caution()
                ))
                .toList());

        return """
                You are an honest car buying assistant for an Indian buyer.
                Write 3 short paragraphs:
                1. Why the top recommendation fits best.
                2. Compare the shortlist with explicit trade-offs using safety, mileage, boot space, owner ratings, and price stretch.
                3. Give two concrete next steps for the buyer.
                Mention one safer alternative or one more practical alternative if relevant.
                Keep it grounded, not salesy, and under 180 words.

                Buyer profile: %s
                Notes: %s
                Shortlist: %s
                Trade-offs: %s
                """.formatted(
                buyerSummary,
                request.notes() == null ? "None" : request.notes(),
                recommendationJson,
                objectMapper.writeValueAsString(tradeOffs)
        );
    }

    private String buildWhyThisCarPrompt(BuyerPreferencesRequest request, List<RecommendationItemDto> recommendations) throws Exception {
        String recommendationJson = objectMapper.writeValueAsString(recommendations.stream()
                .map(item -> Map.of(
                        "id", item.car().id(),
                        "name", item.car().make() + " " + item.car().model() + " " + item.car().variant(),
                        "score", item.score(),
                        "confidenceScore", item.confidenceScore(),
                        "safetyRating", item.car().safetyRating(),
                        "userRating", item.car().userRating(),
                        "bootSpaceLiters", item.car().bootSpaceLiters(),
                        "seatingCapacity", item.car().seatingCapacity(),
                        "reasons", item.reasons(),
                        "caution", item.caution()
                ))
                .toList());

        return """
                You are generating short tooltip copy for a car buying app.
                Return JSON only. Keys must be the car ids provided.
                Each value must be 1 sentence under 28 words explaining why that specific car suits this buyer.
                Mention trade-offs when needed.
                Buyer profile: %s
                Recommendations: %s
                """.formatted(buildBuyerSnapshot(request), recommendationJson);
    }

    private String buildIntentPrompt(String prompt) {
        return """
                Extract structured car-buying preferences from the user's text.
                Return JSON only with this exact shape:
                {
                  "preferences": {
                    "name": "Buyer",
                    "budgetLakh": 15,
                    "primaryUse": "city|highway|mixed",
                    "preferredTransmission": "Any|Manual|Automatic",
                    "fuelPreference": "Any|Petrol|Diesel|Hybrid",
                    "bodyStyle": "Any|SUV|Sedan|Hatchback|Crossover",
                    "familySize": 4,
                    "priority": "Safety|Mileage|Features",
                    "drivingMix": "City|Highway|Mixed",
                    "notes": "original intent summarized briefly",
                    "mustHaveFeatures": []
                  },
                  "signals": ["short bullet", "short bullet"],
                  "source": "gemini"
                }
                Rules:
                - Infer missing values conservatively.
                - Keep enums exactly as allowed above.
                - Budget must be an integer in lakhs.
                - Family size must be 1 to 8.
                User text: %s
                """.formatted(prompt);
    }

    private String generateText(String prompt) throws Exception {
        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .header("x-goog-api-key", geminiApiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return null;
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return null;
        }
        return textNode.asText();
    }

    private String cleanJson(String raw) {
        return raw.replace("```json", "").replace("```", "").trim();
    }

    private String fallbackSummary(List<RecommendationItemDto> recommendations, List<String> tradeOffs) {
        RecommendationItemDto topPick = recommendations.get(0);
        RecommendationItemDto alternative = recommendations.size() > 1 ? recommendations.get(1) : topPick;
        return "%s %s is the best overall fit because it balances confidence, usability, and ownership comfort. %s %s is the stronger alternative if you value its specific edge more, but compare safety rating, boot space, and price stretch before choosing. Next, check on-road pricing and do back-to-back test drives with your family in the rear seat."
                .formatted(topPick.car().make(), topPick.car().model(), alternative.car().make(), alternative.car().model());
    }

    private Map<String, String> buildFallbackWhyTips(List<RecommendationItemDto> recommendations) {
        Map<String, String> tips = new LinkedHashMap<>();
        for (RecommendationItemDto recommendation : recommendations) {
            CarDto car = recommendation.car();
            tips.put(
                    car.id(),
                    "%s fits because it combines %d/5 safety, %.1f owner rating, and %dL boot space with %s."
                            .formatted(car.model(), car.safetyRating(), car.userRating(), car.bootSpaceLiters(), recommendation.caution().replace("Trade-off: ", "").replace(".", ""))
            );
        }
        return tips;
    }

    private String buildBuyerSnapshot(BuyerPreferencesRequest request) {
        return "%s, budget %d lakh, %s priority, %s body style, %s transmission, family size %d, %s driving."
                .formatted(
                        request.name(),
                        request.budgetLakh(),
                        request.priority(),
                        request.bodyStyle(),
                        request.preferredTransmission(),
                        request.familySize(),
                        request.drivingMix()
                );
    }
}
