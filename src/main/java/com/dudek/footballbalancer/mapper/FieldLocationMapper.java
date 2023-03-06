package com.dudek.footballbalancer.mapper;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.entity.FieldLocation;

public interface FieldLocationMapper {

    FieldLocationSimpleDto fieldLocationToSimpleDto(FieldLocation fieldLocation);

    FieldLocation requestDtoToFieldLocation(FieldLocationRequestDto simpleDto);
}
