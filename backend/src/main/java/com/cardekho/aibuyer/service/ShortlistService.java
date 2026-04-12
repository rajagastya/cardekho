package com.cardekho.aibuyer.service;

import com.cardekho.aibuyer.dto.CarDto;
import com.cardekho.aibuyer.dto.SavedShortlistRequest;
import com.cardekho.aibuyer.dto.SavedShortlistResponse;
import com.cardekho.aibuyer.entity.SavedShortlist;
import com.cardekho.aibuyer.repository.SavedShortlistRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ShortlistService {

    private final SavedShortlistRepository repository;
    private final ObjectMapper objectMapper;

    public ShortlistService(SavedShortlistRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public List<SavedShortlistResponse> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(SavedShortlist::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public SavedShortlistResponse save(SavedShortlistRequest request) {
        try {
            SavedShortlist entity = new SavedShortlist();
            entity.setName(request.name());
            entity.setBuyerSummary(request.buyerSummary());
            entity.setCarsJson(objectMapper.writeValueAsString(request.cars()));
            return toResponse(repository.save(entity));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save shortlist", e);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private SavedShortlistResponse toResponse(SavedShortlist entity) {
        try {
            List<CarDto> cars = objectMapper.readValue(entity.getCarsJson(), new TypeReference<>() {});
            return new SavedShortlistResponse(entity.getId(), entity.getName(), entity.getBuyerSummary(), cars, entity.getCreatedAt());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read saved shortlist", e);
        }
    }
}
