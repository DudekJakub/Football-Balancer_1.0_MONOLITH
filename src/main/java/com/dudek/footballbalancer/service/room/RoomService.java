package com.dudek.footballbalancer.service.room;

import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

public interface RoomService {

    default Room obtainRoomFromDbAndCheckAdminPermission(final Long roomId, final Long adminId, final RoomRepository roomRepository) {
        final Room targetRoomFromDb = roomRepository.findByIdFetchAdmins(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isGivenAdminIdAccordant = targetRoomFromDb.getAdminsInRoom().stream()
                .map(User::getId)
                .collect(Collectors.toList())
                .contains(adminId);

        if (!isGivenAdminIdAccordant) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return targetRoomFromDb;
    }
}
