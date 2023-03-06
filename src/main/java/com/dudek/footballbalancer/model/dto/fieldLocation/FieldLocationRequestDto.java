package com.dudek.footballbalancer.model.dto.fieldLocation;

import lombok.*;
import org.springframework.data.geo.Point;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FieldLocationRequestDto {

    @Pattern(regexp = "^[a-zA-Z]+$", message = "City name cannot contain any non-alphabetic characters!")
    private String city;

    @Pattern(regexp = "^((\\d{2}-\\d{3}))$", message = "Zip code pattern: XX-XXX | only digits")
    private String zipCode;

    private String street;

    @Pattern(regexp = "^\\d+$", message = "Number must be at least 1 digit")
    private int number;
    private Point coordinates;
}
