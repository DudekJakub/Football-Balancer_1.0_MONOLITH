package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import org.springframework.stereotype.Component;

@Component
public class FieldLocationMapperImpl implements FieldLocationMapper {

    @Override
    public FieldLocationSimpleDto fieldLocationToSimpleDto(FieldLocation fieldLocation) {
        return FieldLocationSimpleDto.builder()
                .city(fieldLocation.getCity())
                .zipCode(fieldLocation.getZipCode())
                .street(fieldLocation.getStreet())
                .number(fieldLocation.getNumber())
                .latitude(fieldLocation.getLatitude())
                .longitude(fieldLocation.getLongitude())
                .build();
    }

    @Override
    public FieldLocation requestDtoToFieldLocation(FieldLocationRequestDto simpleDto) {
        return FieldLocation.builder()
                .city(simpleDto.getCity())
                .zipCode(simpleDto.getZipCode())
                .street(simpleDto.getStreet())
                .number(simpleDto.getNumber())
                .build();
    }
}
