package com.db.foodara.dto.request.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreCreateRequest {
    @NotBlank(message = "STORE_NAME_REQUIRED")
    @Size(min = 5, message = "STORE_NAME_INVALID")
    private String name;

    @NotBlank(message = "SLUG_REQUIRED")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "SLUG_INVALID_FORMAT")
    private String slug;

    private String addressLine;

    private String ward;

    @NotBlank(message = "DISTRICT_REQUIRED")
    private String districtName;

    @NotBlank(message = "CITY_REQUIRED")
    private String cityName;
    private String latitude;
    private String longitude;
}
