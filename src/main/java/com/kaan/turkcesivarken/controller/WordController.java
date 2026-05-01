package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.entity.Word;
import com.kaan.turkcesivarken.repository.WordRepository;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{slug}")
    public Word getWordBySlug(@PathVariable String slug) {

        return wordRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Kelime bulunamadı"));
    }

    @PostMapping
    public Word saveWord(@RequestBody Word word) {
        return wordRepository.save(word);
    }
}