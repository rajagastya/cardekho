package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.CarDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class CatalogService {

    private final ObjectMapper objectMapper;
    private List<CarDto> cars = List.of();

    public CatalogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCars() {
        try (InputStream inputStream = new ClassPathResource("cars.json").getInputStream()) {
            this.cars = List.copyOf(objectMapper.readValue(inputStream, new TypeReference<>() {}));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load car catalog from cars.json", exception);
        }
    }

    public List<CarDto> getCars() {
        return cars;
    }
}
