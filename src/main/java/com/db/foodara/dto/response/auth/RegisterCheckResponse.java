package com.db.foodara.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCheckResponse {
    private boolean exists;
    private boolean passwordMatched;
    private boolean canLinkRole;
    private String targetRole;
    private List<String> roles;
}
