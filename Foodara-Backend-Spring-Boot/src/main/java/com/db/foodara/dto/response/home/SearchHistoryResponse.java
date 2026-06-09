package com.db.foodara.dto.response.home;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponse {

    private String id;
    private String userId;
    private String searchQuery;
    private String searchType;
    private Integer resultCount;
    private LocalDateTime createdAt;
}