package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.entity.Category;
import com.kaan.turkcesivarken.entity.Word;
import com.kaan.turkcesivarken.repository.CategoryRepository;
import com.kaan.turkcesivarken.repository.WordRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private static final String DEFAULT_CATEGORY_NAME =
            "Diğer";

    private final CategoryRepository categoryRepository;
    private final WordRepository wordRepository;

    public CategoryController(
            CategoryRepository categoryRepository,
            WordRepository wordRepository
    ) {

        this.categoryRepository = categoryRepository;
        this.wordRepository = wordRepository;

    }

    /*
    --------------------------------
    LIST
    --------------------------------
    */

    @GetMapping
    public List<Category> list() {

        return categoryRepository
                .findAllByOrderByNameAsc();

    }

    /*
    --------------------------------
    CREATE
    --------------------------------
    */

    @PostMapping
    @Transactional
    public Category create(
            @RequestBody Map<String, String> request
    ) {

        String name =
                normalizeCategoryName(
                        request.get("name")
                );

        ensureCategoryNameIsAvailable(
                name,
                null
        );

        Category category =
                new Category();

        category.setName(name);

        return categoryRepository.save(category);

    }

    /*
    --------------------------------
    UPDATE
    --------------------------------
    */

    @PutMapping("/{id}")
    @Transactional
    public Category update(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request
    ) {

        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow();

        String trimmedName =
                normalizeCategoryName(
                        request.get("name")
                );

        ensureCategoryNameIsAvailable(
                trimmedName,
                category.getId()
        );

        category.setName(trimmedName);

        return categoryRepository.save(category);

    }

    /*
    --------------------------------
    DELETE
    --------------------------------
    */

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(
            @PathVariable UUID id
    ) {

        Category category =
                categoryRepository
                        .findById(id)
                        .orElseThrow();

        if (
                DEFAULT_CATEGORY_NAME.equalsIgnoreCase(
                        category.getName()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Diğer kategorisi silinemez."
            );

        }

        Category defaultCategory =
                categoryRepository
                        .findByName(DEFAULT_CATEGORY_NAME)
                        .orElseThrow();

        List<Word> words =
                wordRepository
                        .findByCategory(category);

        words.forEach(word ->
                word.setCategory(defaultCategory)
        );

        wordRepository.saveAll(words);
        categoryRepository.delete(category);

    }

    /*
    --------------------------------
    HELPERS
    --------------------------------
    */

    private String normalizeCategoryName(
            String name
    ) {

        if (name == null || name.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kategori adı boş olamaz."
            );

        }

        String trimmedName =
                name.trim();

        return trimmedName
                .substring(0, 1)
                .toUpperCase(
                        Locale.forLanguageTag("tr")
                )
                + trimmedName.substring(1);

    }

    private void ensureCategoryNameIsAvailable(
            String name,
            UUID currentCategoryId
    ) {

        categoryRepository
                .findByName(name)
                .filter(existing ->
                        currentCategoryId == null
                                || !existing
                                .getId()
                                .equals(currentCategoryId)
                )
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Bu kategori zaten var."
                    );
                });

    }

}
