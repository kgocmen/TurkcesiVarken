package com.kaan.turkcesivarken.dto;

import java.util.List;
import java.util.UUID;

public record CategoryWordsResponse(
        UUID id,
        String name,
        List<WordListResponse> words
) {
}
