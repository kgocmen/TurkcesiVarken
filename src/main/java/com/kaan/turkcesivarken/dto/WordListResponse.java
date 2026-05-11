package com.kaan.turkcesivarken.dto;

import java.util.UUID;

public record WordListResponse(
        UUID id,
        String name,
        String slug,
        String origin,
        String category,
        Integer rating
) {
}
