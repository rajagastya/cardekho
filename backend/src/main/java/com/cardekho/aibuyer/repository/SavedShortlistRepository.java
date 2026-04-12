package com.cardekho.aibuyer.repository;

import com.cardekho.aibuyer.entity.SavedShortlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedShortlistRepository extends JpaRepository<SavedShortlist, Long> {
}
