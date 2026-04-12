package com.cardekho.aibuyer.dto;

public record CarDto(
        String id,
        String make,
        String model,
        String variant,
        String bodyType,
        String fuelType,
        int priceLakh,
        int mileage,
        int safetyRating,
        int seatingCapacity,
        String transmission,
        String useCase,
        String highlight,
        String imageUrl,
        double userRating,
        int bootSpaceLiters,
        int reviewCount
) {
}
