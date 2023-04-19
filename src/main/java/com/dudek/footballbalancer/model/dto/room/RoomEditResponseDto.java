package com.dudek.footballbalancer.model.dto.room;

import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEditResponseDto {

    private String name;
    private String description;
    private boolean isPublic;
    private FieldLocationSimpleDto location;

    public RoomEditResponseDto(final Room editedRoom, final FieldLocation editedFieldLocation) {
        this.name = editedRoom.getName();
        this.description = editedRoom.getDescription();
        this.isPublic = editedRoom.isPublic();
        this.location = new FieldLocationSimpleDto();
        this.location.setCity(editedFieldLocation.getCity());
        this.location.setZipCode(editedFieldLocation.getZipCode());
        this.location.setStreet(editedFieldLocation.getStreet());
        this.location.setNumber(editedFieldLocation.getNumber());
        this.location.setLatitude(editedFieldLocation.getLatitude());
        this.location.setLongitude(editedFieldLocation.getLongitude());
    }

    public RoomEditResponseDto(final Room editedRoom) {
        this.name = editedRoom.getName();
        this.description = editedRoom.getDescription();
        this.isPublic = editedRoom.isPublic();
    }
}
