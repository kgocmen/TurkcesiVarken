package com.kaan.turkcesivarken.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "words")
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    @Column(
            nullable = false,
            unique = true
    )
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT")
    private String origin;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Integer rating = 5;

    private Boolean tdk = true;

    /*
    --------------------------------
    CATEGORY
    --------------------------------
    */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    /*
    --------------------------------
    SYNONYMS
    --------------------------------
    */

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "word_synonyms",

            joinColumns = @JoinColumn(
                    name = "word_id"
            ),

            inverseJoinColumns = @JoinColumn(
                    name = "synonym_id"
            )
    )
    @JsonIgnoreProperties({
            "synonyms",
            "similarWords",
            "category"
    })
    private Set<Word> synonyms =
            new HashSet<>();

    /*
    --------------------------------
    SIMILAR WORDS
    --------------------------------
    */

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "word_similar_words",

            joinColumns = @JoinColumn(
                    name = "word_id"
            )
    )
    @Column(name = "similar_word")
    private Set<String> similarWords =
            new HashSet<>();


    /*
    --------------------------------
    HELPERS
    --------------------------------
    */

    public void addSynonym(Word word) {
        this.synonyms.add(word);
    }

    public void addSimilarWord(String word) {
        this.similarWords.add(word);
    }
}