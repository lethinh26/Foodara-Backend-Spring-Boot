package com.db.foodara.dto.response.location;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuggestResponse(
        String id,
        String name,
        String fullAddress
) {}
