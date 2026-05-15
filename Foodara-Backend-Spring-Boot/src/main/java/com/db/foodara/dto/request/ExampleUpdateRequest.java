package com.db.foodara.dto.request;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class ExampleUpdateRequest {
    private String password;
    private String firstName;
    private String lastName;
    private LocalDate dob;

}
