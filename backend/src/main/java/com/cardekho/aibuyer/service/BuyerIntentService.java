package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.BuyerIntentResponse;
import com.cardekho.aibuyer.dto.BuyerPreferencesRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BuyerIntentService {

    private static final Pattern BUDGET_PATTERN = Pattern.compile("(?:under|around|budget|upto|up to|max)?\\s*rs\\.?\\s*(\\d{1,2})\\s*(?:l|lac|lakh|lakhs)?|(\\d{1,2})\\s*(?:l|lac|lakh|lakhs)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FAMILY_PATTERN = Pattern.compile("(?:family of|for|with|we are|seat for)\\s*(\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SEATER_PATTERN = Pattern.compile("(\\d)\\s*-?\\s*seater", Pattern.CASE_INSENSITIVE);

    private final GeminiSummaryService geminiSummaryService;

    public BuyerIntentService(GeminiSummaryService geminiSummaryService) {
        this.geminiSummaryService = geminiSummaryService;
    }

    public BuyerIntentResponse parseIntent(String prompt) {
        BuyerIntentResponse aiResponse = geminiSummaryService.parseBuyerIntent(prompt);
        if (aiResponse != null) {
            return aiResponse;
        }
        return fallbackIntent(prompt);
    }

    private BuyerIntentResponse fallbackIntent(String prompt) {
        String text = prompt.toLowerCase(Locale.ENGLISH);
        List<String> signals = new ArrayList<>();

        int budget = extractNumber(BUDGET_PATTERN.matcher(text), 15);
        if (budget != 15) {
            signals.add("Budget mentioned around Rs. " + budget + " lakh");
        }

        int familySize = extractFamilySize(text);
        if (familySize > 1) {
            signals.add("Family usage appears important");
        }

        String bodyStyle = containsAny(text, "suv", "compact suv") ? "SUV"
                : containsAny(text, "sedan") ? "Sedan"
                : containsAny(text, "hatchback", "small car") ? "Hatchback"
                : containsAny(text, "crossover") ? "Crossover"
                : "Any";

        if (!"Any".equals(bodyStyle)) {
            signals.add("Body style preference detected: " + bodyStyle);
        }

        String fuel = containsAny(text, "diesel") ? "Diesel"
                : containsAny(text, "hybrid") ? "Hybrid"
                : containsAny(text, "petrol") ? "Petrol"
                : "Any";

        String transmission = containsAny(text, "automatic", "amt", "cvt") ? "Automatic"
                : containsAny(text, "manual") ? "Manual"
                : "Any";

        String priority = containsAny(text, "safe", "safety", "crash") ? "Safety"
                : containsAny(text, "mileage", "fuel economy", "efficient", "low maintenance", "running cost") ? "Mileage"
                : containsAny(text, "feature", "tech", "sunroof", "adas", "camera", "ventilated seat") ? "Features"
                : "Safety";

        String drivingMix = containsAny(text, "highway", "road trip", "long drive") ? "Highway"
                : containsAny(text, "city", "traffic", "commute", "bangalore", "delhi", "mumbai", "pune") ? "City"
                : "Mixed";

        String primaryUse = drivingMix.toLowerCase(Locale.ENGLISH);
        if ("mixed".equals(primaryUse) && containsAny(text, "weekend", "office", "daily")) {
            signals.add("Mixed weekday and weekend usage inferred");
        }

        List<String> mustHaveFeatures = extractFeatures(text, signals);

        String name = "Buyer";
        if (signals.isEmpty()) {
            signals.add("Used rule-based extraction because AI parsing was unavailable");
        }

        return new BuyerIntentResponse(
                new BuyerPreferencesRequest(
                        name,
                        budget,
                        primaryUse,
                        transmission,
                        fuel,
                        bodyStyle,
                        familySize,
                        priority,
                        drivingMix,
                        prompt,
                        mustHaveFeatures
                ),
                signals,
                "fallback"
        );
    }

    private int extractNumber(Matcher matcher, int defaultValue) {
        if (matcher.find()) {
            String first = matcher.group(1);
            String second = matcher.group(2);
            return Integer.parseInt(first != null ? first : second);
        }
        return defaultValue;
    }

    private int extractFamilySize(String text) {
        Matcher matcher = FAMILY_PATTERN.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        Matcher seaterMatcher = SEATER_PATTERN.matcher(text);
        if (seaterMatcher.find()) {
            return Integer.parseInt(seaterMatcher.group(1));
        }
        if (containsAny(text, "parents", "kids", "family")) {
            return 4;
        }
        return 2;
    }

    private List<String> extractFeatures(String text, List<String> signals) {
        List<String> features = new ArrayList<>();
        addFeatureIfPresent(text, features, signals, "sunroof", "Sunroof");
        addFeatureIfPresent(text, features, signals, "adas", "ADAS");
        addFeatureIfPresent(text, features, signals, "rear camera", "Rear Camera");
        addFeatureIfPresent(text, features, signals, "ventilated", "Ventilated Seats");
        addFeatureIfPresent(text, features, signals, "boot", "Large Boot");
        addFeatureIfPresent(text, features, signals, "ground clearance", "Ground Clearance");
        return features;
    }

    private void addFeatureIfPresent(String text, List<String> features, List<String> signals, String token, String label) {
        if (text.contains(token)) {
            features.add(label);
            signals.add("Feature preference detected: " + label);
        }
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
