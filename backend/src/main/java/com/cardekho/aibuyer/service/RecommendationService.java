package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.BuyerPreferencesRequest;
import com.cardekho.aibuyer.dto.CarDto;
import com.cardekho.aibuyer.dto.RecommendationItemDto;
import com.cardekho.aibuyer.dto.RecommendationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {

    private static final double MAX_SCORE = 110.0;

    private final CatalogService catalogService;
    private final GeminiSummaryService geminiSummaryService;

    public RecommendationService(CatalogService catalogService, GeminiSummaryService geminiSummaryService) {
        this.catalogService = catalogService;
        this.geminiSummaryService = geminiSummaryService;
    }

    public RecommendationResponse recommend(BuyerPreferencesRequest request) {
        List<ScoredRecommendation> scoredRecommendations = catalogService.getCars().stream()
                .map(car -> scoreCar(car, request))
                .sorted(Comparator.comparingDouble(ScoredRecommendation::score).reversed())
                .limit(5)
                .toList();

        Map<String, String> aiTips = geminiSummaryService.generateWhyThisCarTips(
                request,
                scoredRecommendations.stream().map(ScoredRecommendation::toDtoSkeleton).toList()
        );

        List<RecommendationItemDto> rankedCars = scoredRecommendations.stream()
                .map(item -> item.toDto(aiTips.get(item.car().id())))
                .toList();

        String buyerSummary = buildBuyerSummary(request);
        List<String> tradeOffs = rankedCars.stream()
                .map(item -> item.car().model() + ": " + item.caution())
                .toList();

        String aiSummary = geminiSummaryService.generateSummary(request, rankedCars, buyerSummary, tradeOffs);
        return new RecommendationResponse(buyerSummary, aiSummary, rankedCars, tradeOffs);
    }

    private ScoredRecommendation scoreCar(CarDto car, BuyerPreferencesRequest request) {
        double score = 0;
        List<String> reasons = new ArrayList<>();

        score += scoreBudget(car, request, reasons);
        score += scoreBodyStyle(car, request, reasons);
        score += scoreFuel(car, request, reasons);
        score += scoreTransmission(car, request, reasons);
        score += scorePriority(car, request, reasons);
        score += scoreFamilyFit(car, request, reasons);
        score += scoreDrivingFit(car, request, reasons);
        score += scoreRatingSignals(car, reasons);

        int normalizedScore = (int) Math.round(Math.max(0, Math.min(100, score)));
        int confidenceScore = (int) Math.round(Math.max(35, Math.min(98, (score / MAX_SCORE) * 100)));
        String caution = buildTradeOff(car, request);

        return new ScoredRecommendation(
                car,
                score,
                normalizedScore,
                confidenceScore,
                reasons.stream().distinct().limit(4).toList(),
                caution
        );
    }

    private double scoreBudget(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        if (car.priceLakh() <= request.budgetLakh()) {
            reasons.add("Fits within your budget without stretching the shortlist");
            return 24;
        }
        if (car.priceLakh() <= request.budgetLakh() + 2) {
            reasons.add("Slight stretch, but justified if you value the added strengths");
            return 12;
        }
        return -15;
    }

    private double scoreBodyStyle(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        if (matches(request.bodyStyle(), car.bodyType())) {
            reasons.add("Body style lines up with the shape of car you want");
            return 12;
        }
        return 0;
    }

    private double scoreFuel(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        if (matches(request.fuelPreference(), car.fuelType())) {
            reasons.add("Fuel type matches your ownership preference");
            return 10;
        }
        return 0;
    }

    private double scoreTransmission(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        if (matchesTransmission(request.preferredTransmission(), car.transmission())) {
            reasons.add("Transmission suits the way you expect to drive daily");
            return 8;
        }
        return 0;
    }

    private double scorePriority(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        if ("safety".equalsIgnoreCase(request.priority())) {
            if (car.safetyRating() >= 5) {
                reasons.add("Top-tier safety rating supports your main priority");
                return 16;
            }
            if (car.safetyRating() >= 4) {
                return 10;
            }
        }

        if ("mileage".equalsIgnoreCase(request.priority())) {
            if (car.mileage() >= 22) {
                reasons.add("Mileage is strong for long-term running costs");
                return 16;
            }
            if (car.mileage() >= 19) {
                return 10;
            }
        }

        if ("features".equalsIgnoreCase(request.priority())) {
            double featureSignal = car.userRating() >= 4.5 ? 14 : 9;
            if (featureSignal >= 14) {
                reasons.add("Higher owner ratings suggest a richer everyday experience");
            }
            return featureSignal;
        }

        return 6;
    }

    private double scoreFamilyFit(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        double score = 0;
        if (car.seatingCapacity() >= request.familySize()) {
            score += 6;
        } else {
            score -= 8;
        }

        if (request.familySize() >= 4 && car.bootSpaceLiters() >= 350) {
            reasons.add("Boot space works better for family bags or airport runs");
            score += 6;
        }

        if (request.familySize() >= 6 && car.seatingCapacity() >= 6) {
            reasons.add("Seating capacity genuinely supports a larger family");
            score += 8;
        }

        return score;
    }

    private double scoreDrivingFit(CarDto car, BuyerPreferencesRequest request, List<String> reasons) {
        double score = 0;
        if ("city".equalsIgnoreCase(request.drivingMix()) && isAutomatic(car.transmission())) {
            reasons.add("Automatic-friendly setup helps in stop-go traffic");
            score += 8;
        }
        if ("highway".equalsIgnoreCase(request.drivingMix()) && car.safetyRating() >= 4) {
            reasons.add("Safety and stability profile suits more highway use");
            score += 8;
        }
        if ("mixed".equalsIgnoreCase(request.primaryUse()) && car.mileage() >= 18) {
            score += 5;
        }
        return score;
    }

    private double scoreRatingSignals(CarDto car, List<String> reasons) {
        double score = 0;
        score += car.userRating() * 2.5;
        if (car.reviewCount() >= 500) {
            reasons.add("Large review base makes owner sentiment more trustworthy");
            score += 4;
        } else if (car.reviewCount() >= 200) {
            score += 2;
        }
        return score;
    }

    private boolean matches(String preference, String actual) {
        return "any".equalsIgnoreCase(preference) || actual.equalsIgnoreCase(preference);
    }

    private boolean matchesTransmission(String preference, String actual) {
        return "any".equalsIgnoreCase(preference) || actual.equalsIgnoreCase(preference) ||
                ("automatic".equalsIgnoreCase(preference) && isAutomatic(actual));
    }

    private boolean isAutomatic(String transmission) {
        return transmission.equalsIgnoreCase("Automatic") || transmission.equalsIgnoreCase("CVT");
    }

    private String buildTradeOff(CarDto car, BuyerPreferencesRequest request) {
        List<String> tradeOffs = new ArrayList<>();
        if (car.priceLakh() > request.budgetLakh()) {
            tradeOffs.add("costs more than your target budget");
        }
        if ("safety".equalsIgnoreCase(request.priority()) && car.safetyRating() < 5) {
            tradeOffs.add("safety is good but not best-in-shortlist");
        }
        if ("mileage".equalsIgnoreCase(request.priority()) && car.mileage() < 18) {
            tradeOffs.add("efficiency is acceptable rather than standout");
        }
        if (request.familySize() >= 5 && car.bootSpaceLiters() < 350) {
            tradeOffs.add("boot space may feel tight on packed family trips");
        }
        if (tradeOffs.isEmpty()) {
            tradeOffs.add("the compromise is mainly paying for its stronger all-round balance");
        }
        return "Trade-off: " + String.join(", ", tradeOffs) + ".";
    }

    private String buildBuyerSummary(BuyerPreferencesRequest request) {
        return "%s wants a %s-focused %s under Rs. %d lakh, with %s fuel, %s transmission, and room for %d people."
                .formatted(
                        request.name(),
                        request.priority(),
                        request.bodyStyle().equalsIgnoreCase("any") ? "car" : request.bodyStyle(),
                        request.budgetLakh(),
                        request.fuelPreference(),
                        request.preferredTransmission(),
                        request.familySize()
                );
    }

    private record ScoredRecommendation(
            CarDto car,
            double score,
            int normalizedScore,
            int confidenceScore,
            List<String> reasons,
            String caution
    ) {
        RecommendationItemDto toDtoSkeleton() {
            return new RecommendationItemDto(car, normalizedScore, confidenceScore, reasons, caution, null);
        }

        RecommendationItemDto toDto(String whyThisCarTip) {
            return new RecommendationItemDto(car, normalizedScore, confidenceScore, reasons, caution, whyThisCarTip);
        }
    }
}
