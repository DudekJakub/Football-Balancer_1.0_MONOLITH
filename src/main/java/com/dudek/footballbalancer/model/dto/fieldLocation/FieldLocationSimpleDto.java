package com.dudek.footballbalancer.model.dto.fieldLocation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FieldLocationSimpleDto {

    private String city;
    private String zipCode;
    private String street;
    private int number;
    private double latitude;
    private double longitude;
}
