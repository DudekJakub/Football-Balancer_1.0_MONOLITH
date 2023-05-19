package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldLocationMapperImplTests {

    private final FieldLocationMapperImpl mapper = new FieldLocationMapperImpl();

    @Test
    void fieldLocationToSimpleDto() {
        //given
        FieldLocation fieldLocation = FieldLocation.builder()
                .city("Poznan")
                .zipCode("60-688")
                .street("Łozowa")
                .number(7)
                .latitude(50.00)
                .longitude(30.00)
                .build();

        //when
        FieldLocationSimpleDto simpleDto = mapper.fieldLocationToSimpleDto(fieldLocation);

        //then
        assertEquals(fieldLocation.getCity(), simpleDto.getCity());
        assertEquals(fieldLocation.getZipCode(), simpleDto.getZipCode());
        assertEquals(fieldLocation.getStreet(), simpleDto.getStreet());
        assertEquals(fieldLocation.getNumber(), simpleDto.getNumber());
        assertEquals(fieldLocation.getLatitude(), simpleDto.getLatitude());
        assertEquals(fieldLocation.getLongitude(), simpleDto.getLongitude());
    }

    @Test
    void requestDtoToFieldLocation() {
        //given
        FieldLocationRequestDto requestDto = FieldLocationRequestDto.builder()
                .city("Poznan")
                .street("Lozowa")
                .number(7)
                .zipCode("60-688")
                .build();

        //when
        FieldLocation fieldLocation = mapper.requestDtoToFieldLocation(requestDto);

        //then
        assertEquals(requestDto.getCity(), fieldLocation.getCity());
        assertEquals(requestDto.getStreet(), fieldLocation.getStreet());
        assertEquals(requestDto.getNumber(), fieldLocation.getNumber());
        assertEquals(requestDto.getZipCode(), fieldLocation.getZipCode());
    }
}