package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.entity.Word;
import com.kaan.turkcesivarken.repository.WordRepository;
import com.kaan.turkcesivarken.dto.SearchWordResponse;
import org.springframework.web.bind.annotation.*;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/words")
@CrossOrigin
public class WordController {

    private final WordRepository wordRepository;

    public WordController(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @GetMapping
    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    @GetMapping("/id/{id}")
    public Word getWordById(
            @PathVariable String id
    ) {

        return wordRepository.findById(
                java.util.UUID.fromString(id)
        ).orElseThrow();

    }

    @GetMapping("/{slug}")
    public Word getWordBySlug(@PathVariable String slug) {

        return wordRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Kelime bulunamadı"));
    }

    @GetMapping("/search")
    public List<SearchWordResponse> searchWords(
            @RequestParam String q
    ) {

        return wordRepository
                .findByNameContainingIgnoreCase(q)
                .stream()
                .map(word -> new SearchWordResponse(
                        word.getId(),
                        word.getName(),
                        word.getSlug()
                ))
                .toList();

    }

    @PutMapping("/{id}")
    public Word updateWord(
            @PathVariable String id,
            @RequestBody Word updatedWord
    ) {

        Word word =
                wordRepository.findById(
                        java.util.UUID.fromString(id)
                ).orElseThrow();

        word.setName(updatedWord.getName());
        word.setDefinition(updatedWord.getDefinition());
        word.setOrigin(updatedWord.getOrigin());
        word.setNotes(updatedWord.getNotes());
        word.setRating(updatedWord.getRating());
        word.setTdk(updatedWord.getTdk());
        word.setSynonyms(
                updatedWord.getSynonyms()
        );

        word.setSimilarWords(
                updatedWord.getSimilarWords()
        );

        return wordRepository.save(word);

    }

    @PostMapping
    public Word saveWord(@RequestBody Word word) {
        return wordRepository.save(word);
    }

    @DeleteMapping("/{id}")
    public void deleteWord(
            @PathVariable String id
    ) {

        wordRepository.deleteById(
                java.util.UUID.fromString(id)
        );

    }
}