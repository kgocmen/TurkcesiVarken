package com.kaan.turkcesivarken.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class NisanyanService {

    /*
    --------------------------------
    FETCH WORD
    --------------------------------
    */

    public Map<String, Object> fetchWord(
            String word
    ) {

        try {

            String encodedWord =
                    URLEncoder.encode(
                            word,
                            StandardCharsets.UTF_8
                    );

            String url =
                    "https://www.nisanyansozluk.com/api/words/"
                            + encodedWord
                            + "?session=1";

            RestTemplate restTemplate =
                    new RestTemplate();

            Map<String, Object> response =
                    restTemplate.getForObject(
                            url,
                            Map.class
                    );

            return parseResponse(response);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Nişanyan verisi alınamadı: "
                            + e.getMessage()
            );

        }

    }

    /*
    --------------------------------
    PARSE RESPONSE
    --------------------------------
    */

    private Map<String, Object> parseResponse(
            Map<String, Object> response
    ) {

        /*
        --------------------------------
        WORDS
        --------------------------------
        */

        Object wordsObj =
                response.get("words");

        if (!(wordsObj instanceof List<?> wordsList)) {

            throw new RuntimeException(
                    "words listesi bulunamadı."
            );

        }

        if (wordsList.isEmpty()) {

            throw new RuntimeException(
                    "Kelime bulunamadı."
            );

        }

        Map<String, Object> word =
                (Map<String, Object>)
                        wordsList.get(0);

        /*
        --------------------------------
        ETYMOLOGIES
        --------------------------------
        */

        List<Map<String, Object>> etymologies =
                new ArrayList<>();

        Object etymologiesObj =
                word.get("etymologies");

        if (etymologiesObj instanceof List<?> list) {

            for (Object item : list) {

                if (item instanceof Map<?, ?> map) {

                    etymologies.add(
                            (Map<String, Object>) map
                    );

                }

            }

        }

        /*
        --------------------------------
        ORIGIN
        --------------------------------
        */

        List<String> languageChain =
                new ArrayList<>();

        /*
        --------------------------------
        NOTES
        --------------------------------
        */

        StringBuilder notes =
                new StringBuilder();

        for (
                Map<String, Object> etymology
                : etymologies
        ) {

            /*
            ------------------------
            LANGUAGE
            ------------------------
            */

            String language = "";

            Object languagesObj =
                    etymology.get("languages");

            if (languagesObj instanceof List<?> languages) {

                if (!languages.isEmpty()) {

                    Object firstLang =
                            languages.get(0);

                    if (firstLang instanceof Map<?, ?> langMap) {

                        Object langName =
                                langMap.get("name");

                        if (langName != null) {

                            language =
                                    String.valueOf(
                                            langName
                                    );

                            languageChain.add(
                                    language
                            );

                        }

                    }

                }

            }

            /*
            ------------------------
            RELATION
            ------------------------
            */

            String relationText = "";

            Object relationObj =
                    etymology.get("relation");

            if (relationObj instanceof Map<?, ?> relation) {

                Object text =
                        relation.get("text");

                if (text != null) {

                    relationText =
                            String.valueOf(text);

                }

            }

            /*
            ------------------------
            WORD
            ------------------------
            */

            String romanizedText =
                    String.valueOf(
                            etymology.get(
                                    "romanizedText"
                            )
                    );

            /*
            ------------------------
            DEFINITION
            ------------------------
            */

            String definition =
                    String.valueOf(
                            etymology.get(
                                    "definition"
                            )
                    );

            if (
                    definition.equals("null")
            ) {

                definition = "";

            }

            /*
            ------------------------
            BUILD NOTE
            ------------------------
            */

            if (
                    !language.isBlank() &&
                            !romanizedText.equals("null")
            ) {

                notes
                        .append(language)
                        .append(" ")
                        .append(romanizedText);

                if (
                        !definition.isBlank() &&
                                !definition.equals("a.a.")
                ) {

                    notes
                            .append(" “")
                            .append(definition)
                            .append("”");

                }

                notes
                        .append(" sözcüğün")
                        .append(relationText)
                        .append(" ");

            }

        }

        /*
        --------------------------------
        EXTRA NOTE
        --------------------------------
        */

        Object noteObj =
                word.get("note");

        if (noteObj != null) {

            String apiNote =
                    cleanText(
                            String.valueOf(noteObj)
                    );

            notes.append(" ")
                    .append(apiNote);

        }

        /*
        --------------------------------
        SIMILAR WORDS
        --------------------------------
        */

        Set<String> similarWords =
                new LinkedHashSet<>();

        /*
        --------------------------------
        referenceOf
        --------------------------------
        */

        extractWordNames(
                word.get("referenceOf"),
                similarWords
        );

        /*
        --------------------------------
        references
        --------------------------------
        */

        extractWordNames(
                word.get("references"),
                similarWords
        );

        /*
        --------------------------------
        similarWords
        --------------------------------
        */

        extractWordNames(
                word.get("similarWords"),
                similarWords
        );

        /*
        --------------------------------
        DEFINITION
        --------------------------------
        */

        String definition = "";

        if (!etymologies.isEmpty()) {

            Object def =
                    etymologies
                            .get(0)
                            .get("definition");

            if (def != null) {

                definition =
                        String.valueOf(def);

            }

        }

        /*
        --------------------------------
        RESULT
        --------------------------------
        */

        Map<String, Object> result =
                new HashMap<>();

        result.put(
                "name",
                word.get("name")
        );

        result.put(
                "definition",
                definition
        );

        result.put(
                "origin",
                languageChain.isEmpty()
                        ? ""
                        : languageChain.get(0)
        );

        result.put(
                "notes",
                cleanText(
                        notes.toString()
                )
        );

        result.put(
                "similarWords",
                similarWords
        );

        return result;

    }

    /*
    --------------------------------
    EXTRACT WORD NAMES
    --------------------------------
    */

    private void extractWordNames(
            Object obj,
            Set<String> target
    ) {

        if (!(obj instanceof List<?> list)) {
            return;
        }

        for (Object item : list) {

            /*
            ------------------------
            MAP OBJECT
            {"name":"mesa"}
            ------------------------
            */

            if (item instanceof Map<?, ?> map) {

                Object name =
                        map.get("name");

                if (name != null) {

                    target.add(
                            String.valueOf(name)
                    );

                }

            }

            /*
            ------------------------
            STRING OBJECT
            "mesa"
            ------------------------
            */

            else if (item != null) {

                target.add(
                        String.valueOf(item)
                );

            }

        }

    }

    /*
    --------------------------------
    CLEAN TEXT
    --------------------------------
    */

    private String cleanText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text

                .replace("%b", "")
                .replace("%i", "")
                .replace("%u", "")
                .replace("*", "")

                .replace("\"", "”")

                .replaceAll(
                        "\\s+",
                        " "
                )

                .trim();

    }

}