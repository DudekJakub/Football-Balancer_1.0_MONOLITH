package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.validation.customAnnotation.RoomAdminId;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

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

    @NotNull
    @Positive
    @RoomAdminId
    private Long roomAdminId;
}
