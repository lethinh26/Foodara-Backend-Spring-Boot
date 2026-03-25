package com.db.foodara.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class ExampleCreationRequest {
    @Size(min = 3, message = "Username must be at least 3 character") // size -> giới hạn độ dài của chuổi
    private String username;

    @Size(min = 8, message = "Password must be at least 8 character")
    private String password;
    private String firstName;
    private String lastName;

    private LocalDate dob;

}
