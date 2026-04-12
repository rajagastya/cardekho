package com.cardekho.aibuyer.controller;

import com.cardekho.aibuyer.dto.BuyerIntentRequest;
import com.cardekho.aibuyer.dto.BuyerIntentResponse;
import com.cardekho.aibuyer.dto.BuyerPreferencesRequest;
import com.cardekho.aibuyer.dto.RecommendationResponse;
import com.cardekho.aibuyer.dto.SavedShortlistRequest;
import com.cardekho.aibuyer.dto.SavedShortlistResponse;
import com.cardekho.aibuyer.service.BuyerIntentService;
import com.cardekho.aibuyer.service.CatalogService;
import com.cardekho.aibuyer.service.RecommendationService;
import com.cardekho.aibuyer.service.ShortlistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CatalogService catalogService;
    private final RecommendationService recommendationService;
    private final ShortlistService shortlistService;
    private final BuyerIntentService buyerIntentService;

    public CarController(CatalogService catalogService,
                         RecommendationService recommendationService,
                         ShortlistService shortlistService,
                         BuyerIntentService buyerIntentService) {
        this.catalogService = catalogService;
        this.recommendationService = recommendationService;
        this.shortlistService = shortlistService;
        this.buyerIntentService = buyerIntentService;
    }

    @GetMapping("/cars")
    public List<?> getCars() {
        return catalogService.getCars();
    }

    @GetMapping("/meta")
    public Map<String, Object> getMeta() {
        return Map.of(
                "bodyStyles", List.of("Any", "SUV", "Sedan", "Hatchback", "Crossover"),
                "fuelTypes", List.of("Any", "Petrol", "Diesel", "Hybrid"),
                "transmissions", List.of("Any", "Manual", "Automatic"),
                "priorities", List.of("Safety", "Mileage", "Features"),
                "drivingMixes", List.of("City", "Highway", "Mixed")
        );
    }

    @PostMapping("/recommendations")
    public RecommendationResponse getRecommendations(@Valid @RequestBody BuyerPreferencesRequest request) {
        return recommendationService.recommend(request);
    }

    @PostMapping("/ai/intake")
    public BuyerIntentResponse parseBuyerIntent(@Valid @RequestBody BuyerIntentRequest request) {
        return buyerIntentService.parseIntent(request.prompt());
    }

    @GetMapping("/shortlists")
    public List<SavedShortlistResponse> getShortlists() {
        return shortlistService.getAll();
    }

    @PostMapping("/shortlists")
    public SavedShortlistResponse saveShortlist(@Valid @RequestBody SavedShortlistRequest request) {
        return shortlistService.save(request);
    }

    @DeleteMapping("/shortlists/{id}")
    public void deleteShortlist(@PathVariable Long id) {
        shortlistService.delete(id);
    }
}
