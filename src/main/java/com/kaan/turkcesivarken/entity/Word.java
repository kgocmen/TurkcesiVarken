package com.kaan.turkcesivarken.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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

    private Integer rating = 0;

    private Boolean tdk = false;

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
    CONSTRUCTOR
    --------------------------------
    */

    public Word() {
    }

    /*
    --------------------------------
    AUTO SLUG
    --------------------------------
    */

    @PrePersist
    @PreUpdate
    public void generateSlug() {

        if (name == null) {
            return;
        }

        String normalized =
                Normalizer.normalize(
                                name,
                                Normalizer.Form.NFD
                        )
                        .replaceAll(
                                "\\p{InCombiningDiacriticalMarks}+",
                                ""
                        );

        this.slug = normalized
                .toLowerCase(
                        Locale.forLanguageTag("tr")
                )
                .replaceAll(
                        "[^a-z0-9\\s]",
                        ""
                )
                .replaceAll(
                        "\\s+",
                        "-"
                );

    }

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

    /*
    --------------------------------
    GETTERS / SETTERS
    --------------------------------
    */

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Boolean getTdk() {
        return tdk;
    }

    public void setTdk(Boolean tdk) {
        this.tdk = tdk;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Word> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<Word> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<String> getSimilarWords() {
        return similarWords;
    }

    public void setSimilarWords(
            Set<String> similarWords
    ) {
        this.similarWords = similarWords;
    }

}