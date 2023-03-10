package com.dudek.footballbalancer.model.dto.room;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RoomNewDatesRequestDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String nextMatchDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String nextMatchRegistrationStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private String nextMatchRegistrationEndDate;
}
