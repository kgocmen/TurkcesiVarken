package com.kaan.turkcesivarken.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Column(columnDefinition = "TEXT")
    private String origin;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Integer rating;

    private Boolean tdk;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "word_synonyms",

            joinColumns = @JoinColumn(name = "word_id"),

            inverseJoinColumns = @JoinColumn(name = "synonym_id")
    )
    @JsonIgnoreProperties({
            "synonyms",
            "similarWords",
            "category"
    })
    private Set<Word> synonyms = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "word_similar_words",

            joinColumns = @JoinColumn(name = "word_id"),

            inverseJoinColumns = @JoinColumn(name = "similar_word_id")
    )
    @JsonIgnoreProperties({
            "synonyms",
            "similarWords",
            "category"
    })
    private Set<Word> similarWords = new HashSet<>();

    public Word() {
    }

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

    public Set<Word> getSimilarWords() {
        return similarWords;
    }

    public void setSimilarWords(Set<Word> similarWords) {
        this.similarWords = similarWords;
    }
}