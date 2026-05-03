package com.kaan.turkcesivarken.repository;

import com.kaan.turkcesivarken.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WordRepository
        extends JpaRepository<Word, UUID> {

    Optional<Word> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Word> findTop10ByNameContainingIgnoreCaseOrderByNameAsc(
            String name
    );

    List<Word> findByNameIn(
            List<String> names
    );

}
