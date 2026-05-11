package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.dto.CategoryWordsResponse;
import com.kaan.turkcesivarken.dto.SearchWordResponse;
import com.kaan.turkcesivarken.dto.WordListResponse;
import com.kaan.turkcesivarken.entity.Category;
import com.kaan.turkcesivarken.entity.Word;
import com.kaan.turkcesivarken.repository.CategoryRepository;
import com.kaan.turkcesivarken.repository.WordRepository;
import com.kaan.turkcesivarken.service.NisanyanService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/words")
@CrossOrigin
public class WordController {

    private final WordRepository wordRepository;
    private final CategoryRepository categoryRepository;
    private final NisanyanService nisanyanService;

    public WordController(
            WordRepository wordRepository,
            CategoryRepository categoryRepository,
            NisanyanService nisanyanService
    ) {

        this.wordRepository = wordRepository;
        this.categoryRepository = categoryRepository;
        this.nisanyanService = nisanyanService;

    }

    /*
    --------------------------------
    CREATE
    --------------------------------
    */

    @PostMapping
    @Transactional
    public Word create(
            @RequestBody Word word
    ) {

        /*
        ------------------------
        DEFAULT CATEGORY
        ------------------------
        */

        if (
                word.getCategory() == null
                        || (
                        word.getCategory().getId() == null
                                && (
                                word.getCategory().getName() == null
                                        || word.getCategory()
                                        .getName()
                                        .isBlank()
                        )
                )
        ) {

            word.setCategory(defaultCategory());

        } else {

            word.setCategory(
                    resolveCategory(
                            word.getCategory()
                    )
            );

        }

        /*
        ------------------------
        DEFAULT RATING
        ------------------------
        */

        if (word.getRating() == null) {

            word.setRating(5);

        }

        /*
        ------------------------
        DEFAULT TDK
        ------------------------
        */

        if (word.getTdk() == null) {

            word.setTdk(true);

        }

        /*
        ------------------------
        AUTO SLUG
        ------------------------
        */

        String baseSlug =
                slugify(word.getName());

        String finalSlug =
                baseSlug;

        int counter = 2;

        while (
                wordRepository
                        .findBySlug(finalSlug)
                        .isPresent()
        ) {

            finalSlug =
                    baseSlug + counter;

            counter++;

        }

        word.setSlug(finalSlug);

        Set<Word> synonyms =
                resolveSynonyms(
                        word.getSynonyms(),
                        null,
                        word.getName()
                );

        word.setSynonyms(synonyms);

        Word saved =
                wordRepository.save(word);

        syncReverseSynonyms(
                saved,
                Set.of(),
                synonyms
        );

        return saved;

    }

    /*
    --------------------------------
    LIST ALL
    --------------------------------
    */

    @GetMapping("/all")
    public List<WordListResponse> listAll() {

        return wordRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toWordListResponse)
                .toList();

    }

    /*
    --------------------------------
    LIST BY CATEGORIES
    --------------------------------
    */

    @GetMapping("/categories")
    public List<CategoryWordsResponse> listCategories() {

        List<WordListResponse> words =
                wordRepository
                        .findAllByOrderByNameAsc()
                        .stream()
                        .map(this::toWordListResponse)
                        .toList();

        return categoryRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(category ->
                        new CategoryWordsResponse(
                                category.getId(),
                                category.getName(),
                                words
                                        .stream()
                                        .filter(word ->
                                                category
                                                        .getName()
                                                        .equals(
                                                                word.category()
                                                        )
                                        )
                                        .toList()
                        )
                )
                .toList();

    }

    /*
    --------------------------------
    SEARCH
    --------------------------------
    */

    @GetMapping("/search")
    public List<SearchWordResponse> search(
            @RequestParam String q
    ) {

        String query =
                q.trim();

        if (query.length() < 2) {
            return List.of();
        }

        return wordRepository
                .findTop10ByNameContainingIgnoreCaseOrderByNameAsc(query)
                .stream()
                .map(word ->
                        new SearchWordResponse(
                                word.getId(),
                                word.getName(),
                                word.getSlug()
                        )
                )
                .toList();

    }

    /*
    --------------------------------
    GET BY ID
    --------------------------------
    */

    @GetMapping("/id/{id}")
    public Word getById(
            @PathVariable UUID id
    ) {

        return wordRepository
                .findById(id)
                .orElseThrow();

    }

    /*
    --------------------------------
    GET BY SLUG
    --------------------------------
    */

    @GetMapping("/{slug}")
    public Word getBySlug(
            @PathVariable String slug
    ) {

        return wordRepository
                .findBySlug(slug)
                .orElseThrow();

    }

    /*
    --------------------------------
    UPDATE
    --------------------------------
    */

    @PutMapping("/{id}")
    @Transactional
    public Word update(
            @PathVariable UUID id,
            @RequestBody Word updated
    ) {

        Word word =
                wordRepository
                        .findById(id)
                        .orElseThrow();

        word.setName(updated.getName());

        word.setDefinition(
                updated.getDefinition()
        );

        word.setOrigin(
                updated.getOrigin()
        );

        word.setNotes(
                updated.getNotes()
        );

        word.setRating(
                updated.getRating()
        );

        word.setTdk(
                updated.getTdk()
        );

        Set<Word> oldSynonyms =
                new HashSet<>(
                        word.getSynonyms()
                );

        Set<Word> newSynonyms =
                resolveSynonyms(
                        updated.getSynonyms(),
                        word.getId(),
                        updated.getName()
                );

        word.setSynonyms(newSynonyms);

        word.setSimilarWords(
                updated.getSimilarWords()
        );

        /*
        ------------------------
        CATEGORY
        ------------------------
        */

        if (
                updated.getCategory() == null
                        || (
                        updated.getCategory().getId() == null
                                && (
                                updated.getCategory().getName() == null
                                        || updated.getCategory()
                                        .getName()
                                        .isBlank()
                        )
                )
        ) {

            word.setCategory(defaultCategory());

        } else {

            word.setCategory(
                    resolveCategory(
                            updated.getCategory()
                    )
            );

        }

        /*
        ------------------------
        AUTO SLUG
        ------------------------
        */

        String baseSlug =
                slugify(updated.getName());

        String finalSlug =
                baseSlug;

        int counter = 2;

        while (
                wordRepository
                        .findBySlug(finalSlug)
                        .isPresent()
                        &&
                        !word.getSlug().equals(finalSlug)
        ) {

            finalSlug =
                    baseSlug + counter;

            counter++;

        }

        word.setSlug(finalSlug);

        Word saved =
                wordRepository.save(word);

        syncReverseSynonyms(
                saved,
                oldSynonyms,
                newSynonyms
        );

        return saved;

    }

    /*
    --------------------------------
    UPDATE SUMMARY
    --------------------------------
    */

    @PatchMapping("/{id}/summary")
    @Transactional
    public WordListResponse updateSummary(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updated
    ) {

        Word word =
                wordRepository
                        .findById(id)
                        .orElseThrow();

        String name =
                asString(
                        updated.get("name")
                ).trim();

        if (name.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Kelime adı boş olamaz."
            );

        }

        wordRepository
                .findByNameIgnoreCase(name)
                .filter(existing ->
                        !existing
                                .getId()
                                .equals(word.getId())
                )
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Bu kelime zaten var."
                    );
                });

        word.setName(name);

        String slug =
                asString(
                        updated.get("slug")
                ).trim();

        if (slug.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Slug boş olamaz."
            );

        }

        String normalizedSlug =
                slug.equals(word.getSlug())
                        ? word.getSlug()
                        : slugify(slug);

        if (normalizedSlug.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Slug geçerli karakter içermeli."
            );

        }

        if (!normalizedSlug.equals(word.getSlug())) {

            wordRepository
                    .findBySlug(normalizedSlug)
                    .filter(existing ->
                            !existing
                                    .getId()
                                    .equals(word.getId())
                    )
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Bu slug zaten var."
                        );
                    });

        }

        word.setSlug(
                normalizedSlug
        );

        word.setOrigin(
                asString(
                        updated.get("origin")
                ).trim()
        );

        word.setRating(
                parseRating(
                        updated.get("rating")
                )
        );

        String categoryName =
                asString(
                        updated.get("category")
                ).trim();

        if (categoryName.isBlank()) {

            word.setCategory(
                    defaultCategory()
            );

        } else {

            Category category =
                    new Category();

            category.setName(categoryName);

            word.setCategory(
                    resolveCategory(category)
            );

        }

        return toWordListResponse(
                wordRepository.save(word)
        );

    }

    /*
    --------------------------------
    DELETE
    --------------------------------
    */

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {

        wordRepository.deleteById(id);

    }

    /*
    --------------------------------
    SLUGIFY
    --------------------------------
    */

    private String slugify(
            String text
    ) {

        text =
                text.toLowerCase(
                        Locale.forLanguageTag("tr")
                );

        text =
                text
                        .replace("ç", "c")
                        .replace("ğ", "g")
                        .replace("ı", "i")
                        .replace("ö", "o")
                        .replace("ş", "s")
                        .replace("ü", "u");

        text =
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                );

        text =
                text.replaceAll(
                        "[^a-z0-9]",
                        ""
                );

        return text;

    }

    /*
    --------------------------------
    SYNONYM HELPERS
    --------------------------------
    */

    private Set<Word> resolveSynonyms(
            Set<Word> requestedSynonyms,
            UUID ownerId,
            String ownerName
    ) {

        Set<Word> resolvedSynonyms =
                new HashSet<>();

        if (requestedSynonyms == null) {
            return resolvedSynonyms;
        }

        for (Word requested : requestedSynonyms) {

            if (
                    isSameRequestedName(
                            requested,
                            ownerName
                    )
            ) {
                continue;
            }

            Word synonym =
                    resolveSynonym(
                            requested
                    );

            if (
                    synonym == null
                            || isSameWord(
                                    synonym,
                                    ownerId,
                                    ownerName
                            )
            ) {
                continue;
            }

            resolvedSynonyms.add(synonym);

        }

        return resolvedSynonyms;

    }

    private Word resolveSynonym(
            Word requested
    ) {

        if (requested == null) {
            return null;
        }

        if (requested.getId() != null) {

            return wordRepository
                    .findById(requested.getId())
                    .orElseThrow();

        }

        String name =
                requested.getName();

        if (name == null || name.isBlank()) {
            return null;
        }

        String synonymName =
                name.trim();

        return wordRepository
                .findByNameIgnoreCase(synonymName)
                .orElseGet(() ->
                        createFromNisanyan(synonymName)
                );

    }

    private boolean isSameRequestedName(
            Word requested,
            String ownerName
    ) {

        return requested != null
                && requested.getId() == null
                && requested.getName() != null
                && ownerName != null
                && requested
                .getName()
                .trim()
                .equalsIgnoreCase(
                        ownerName.trim()
                );

    }

    private Word createFromNisanyan(
            String name
    ) {

        Map<String, Object> data =
                nisanyanService.fetchWord(name);

        String fetchedName =
                asString(data.get("name"));

        Word word =
                new Word();

        word.setName(
                fetchedName.isBlank()
                        ? name
                        : fetchedName
        );

        Word existingWord =
                wordRepository
                        .findByNameIgnoreCase(
                                word.getName()
                        )
                        .orElse(null);

        if (existingWord != null) {
            return existingWord;
        }

        word.setDefinition(
                asString(data.get("definition"))
        );

        word.setOrigin(
                asString(data.get("origin"))
        );

        word.setNotes(
                asString(data.get("notes"))
        );

        word.setRating(5);
        word.setTdk(true);
        word.setCategory(defaultCategory());
        word.setSlug(
                uniqueSlugFor(
                        word.getName(),
                        null
                )
        );

        Object similarWords =
                data.get("similarWords");

        if (similarWords instanceof Collection<?> collection) {

            for (Object similarWord : collection) {

                if (similarWord != null) {

                    word.addSimilarWord(
                            String.valueOf(
                                    similarWord
                            )
                    );

                }

            }

        }

        return wordRepository.save(word);

    }

    private void syncReverseSynonyms(
            Word word,
            Set<Word> oldSynonyms,
            Set<Word> newSynonyms
    ) {

        Set<Word> changedWords =
                new HashSet<>();

        for (Word oldSynonym : oldSynonyms) {

            if (
                    !containsWord(
                            newSynonyms,
                            oldSynonym
                    )
            ) {

                oldSynonym
                        .getSynonyms()
                        .removeIf(synonym ->
                                isSameWord(
                                        synonym,
                                        word.getId(),
                                        word.getName()
                                )
                        );

                changedWords.add(oldSynonym);

            }

        }

        for (Word newSynonym : newSynonyms) {

            newSynonym.addSynonym(word);
            changedWords.add(newSynonym);

        }

        if (!changedWords.isEmpty()) {
            wordRepository.saveAll(changedWords);
        }

    }

    private boolean isSameWord(
            Word word,
            UUID ownerId,
            String ownerName
    ) {

        if (
                ownerId != null
                        && ownerId.equals(word.getId())
        ) {
            return true;
        }

        return ownerName != null
                && word.getName() != null
                && ownerName.trim()
                .equalsIgnoreCase(
                        word.getName().trim()
                );

    }

    private boolean containsWord(
            Set<Word> words,
            Word target
    ) {

        if (target == null) {
            return false;
        }

        return words
                .stream()
                .anyMatch(word ->
                        isSameWord(
                                word,
                                target.getId(),
                                target.getName()
                        )
                );

    }

    private Category defaultCategory() {

        return categoryRepository
                .findByName("Diğer")
                .orElseThrow();

    }

    private Category resolveCategory(
            Category requestedCategory
    ) {

        if (requestedCategory.getId() != null) {

            return categoryRepository
                    .findById(requestedCategory.getId())
                    .orElseThrow();

        }

        String name =
                normalizeCategoryName(
                        requestedCategory.getName()
                );

        return categoryRepository
                .findByName(name)
                .orElseGet(() -> {

                    Category category =
                            new Category();

                    category.setName(name);

                    return categoryRepository
                            .save(category);

                });

    }

    private String normalizeCategoryName(
            String name
    ) {

        String trimmedName =
                name.trim();

        return trimmedName
                .substring(0, 1)
                .toUpperCase(
                        Locale.forLanguageTag("tr")
                )
                + trimmedName.substring(1);

    }

    private String uniqueSlugFor(
            String name,
            UUID existingWordId
    ) {

        String baseSlug =
                slugify(name);

        String finalSlug =
                baseSlug;

        int counter = 2;

        while (
                wordRepository
                        .findBySlug(finalSlug)
                        .filter(existingWord ->
                                !existingWord
                                        .getId()
                                        .equals(existingWordId)
                        )
                        .isPresent()
        ) {

            finalSlug =
                    baseSlug + counter;

            counter++;

        }

        return finalSlug;

    }

    private String asString(
            Object value
    ) {

        if (value == null) {
            return "";
        }

        return String.valueOf(value);

    }

    private Integer parseRating(
            Object value
    ) {

        if (value == null) {
            return 0;
        }

        try {

            int rating =
                    Integer.parseInt(
                            String.valueOf(value)
                    );

            return Math.max(
                    0,
                    Math.min(
                            5,
                            rating
                    )
            );

        } catch (NumberFormatException e) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rating sayısal olmalı."
            );

        }

    }

    private WordListResponse toWordListResponse(
            Word word
    ) {

        return new WordListResponse(
                word.getId(),
                word.getName(),
                word.getSlug(),
                word.getOrigin(),
                word.getCategory() == null
                        ? ""
                        : word.getCategory().getName(),
                word.getRating()
        );

    }

}
