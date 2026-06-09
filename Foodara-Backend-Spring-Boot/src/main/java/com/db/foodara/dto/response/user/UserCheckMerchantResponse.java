package com.db.foodara.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckMerchantResponse {
    private String userId;
    private String email;
    private String fullName;
    private boolean checkMerchant;
    private String avatarUrl;
}
