package com.db.foodara.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignDriverRequest {

    @NotBlank(message = "Driver ID is required")
    private String driverId;
}
