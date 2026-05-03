package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.dto.SearchWordResponse;
import com.kaan.turkcesivarken.entity.Category;
import com.kaan.turkcesivarken.entity.Word;
import com.kaan.turkcesivarken.repository.CategoryRepository;
import com.kaan.turkcesivarken.repository.WordRepository;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/words")
@CrossOrigin
public class WordController {

    private final WordRepository wordRepository;
    private final CategoryRepository categoryRepository;

    public WordController(
            WordRepository wordRepository,
            CategoryRepository categoryRepository
    ) {

        this.wordRepository = wordRepository;
        this.categoryRepository = categoryRepository;

    }

    /*
    --------------------------------
    CREATE
    --------------------------------
    */

    @PostMapping
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
                        || word.getCategory().getId() == null
        ) {

            Category defaultCategory =
                    categoryRepository
                            .findByName("Diğer")
                            .orElseThrow();

            word.setCategory(defaultCategory);

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

        return wordRepository.save(word);

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

        word.setSynonyms(
                updated.getSynonyms()
        );

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
                        || updated.getCategory().getId() == null
        ) {

            Category defaultCategory =
                    categoryRepository
                            .findByName("Diğer")
                            .orElseThrow();

            word.setCategory(defaultCategory);

        } else {

            word.setCategory(
                    updated.getCategory()
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

        return wordRepository.save(word);

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

}
