package com.nicolas.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String dateOfBirth;
}
