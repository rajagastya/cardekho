package com.cardekho.aibuyer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "saved_shortlists")
public class SavedShortlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1200)
    private String buyerSummary;

    @Lob
    @Column(nullable = false)
    private String carsJson;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuyerSummary() {
        return buyerSummary;
    }

    public void setBuyerSummary(String buyerSummary) {
        this.buyerSummary = buyerSummary;
    }

    public String getCarsJson() {
        return carsJson;
    }

    public void setCarsJson(String carsJson) {
        this.carsJson = carsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
