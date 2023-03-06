package com.dudek.footballbalancer.service;

import com.dudek.footballbalancer.config.security.PasswordEncryptor;
import com.dudek.footballbalancer.mapper.FieldLocationMapper;
import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final FieldLocationMapper fieldLocationMapper;
    private final PasswordEncryptor passwordEncryptor;

    @Autowired
    public RoomService(final RoomRepository roomRepository, final UserRepository userRepository, final RoomMapper roomMapper, final FieldLocationMapper fieldLocationMapper, final PasswordEncryptor passwordEncryptor) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
        this.fieldLocationMapper = fieldLocationMapper;
        this.passwordEncryptor = passwordEncryptor;
    }

    public List<RoomSimpleDto> findPaginated(int pageNumber, int pageSize, Sort.Direction sortDirection, String sortField, boolean fetchPublic, Long userId) {
        Sort sortBy = sortDirection.equals(Sort.Direction.ASC) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortBy);
        List<Room> paginated = roomRepository.findPaginatedFetchUsersInRoomAndLocation(pageable, fetchPublic, userId).toList();
        return roomMapper.roomCollectionToRoomSimpleDtoList(paginated, userId);
    }

    public List<RoomSimpleDto> findPaginatedByUserId(int pageNumber, int pageSize, Sort.Direction sortDirection, String sortField, Long userId) {
        Sort sortBy = sortDirection.equals(Sort.Direction.ASC) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortBy);
        List<Room> paginated = roomRepository.findPaginatedFetchUsersInRoomAndLocationByUserId(pageable, userId).toList();
        return roomMapper.roomCollectionToRoomSimpleDtoList(paginated, userId);
    }

    public RoomEnteredResponseDto enterRoom(final RoomEnterRequestDto requestDto){
        final Room targetRoomFromDb = roomRepository.findByIdFetchPlayersAndUsers(requestDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isUserAlreadyInRoom = targetRoomFromDb.getUsersInRoom().stream()
                .anyMatch(u -> u.getId().equals(requestDto.getUserId()));

        if (isUserAlreadyInRoom) {
            return roomMapper.roomToRoomEnteredResponseDto(targetRoomFromDb);
        }

        if (!passwordEncryptor.checkPasswordMatch(targetRoomFromDb.getPassword(), requestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong password!");
        }

        return roomMapper.roomToRoomEnteredResponseDto(targetRoomFromDb);
    }

    @Transactional
    public RoomNewResponseDto createRoom(final RoomNewRequestDto requestDto) {
        User requestSender = userRepository.findById(requestDto.getUserRequestSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String encryptedPassword = passwordEncryptor.encrypt(requestDto.getRoomPassword())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong! Please try later."));

        final Room newRoom = Room.builder()
                .name(requestDto.getRoomName())
                .password(encryptedPassword)
                .isPublic(requestDto.isPublic())
                .adminsInRoom(Set.of(requestSender))
                .usersInRoom(Set.of(requestSender))
                .build();

        if (requestDto.getLocation() != null) {
            FieldLocation roomLocation = fieldLocationMapper.requestDtoToFieldLocation(requestDto.getLocation());
            newRoom.setFieldLocation(roomLocation);
        } else {
            newRoom.setFieldLocation(FieldLocation.builder()
                    .city("N/A")
                    .zipCode("00-000")
                    .street("N/A")
                    .number(0)
                    .build());
        }

        return roomMapper.roomToRoomNewResponseDto(roomRepository.save(newRoom));
    }

    @Transactional
    public void updateRoom(final Long roomId, final RoomEditRequestDto requestDto) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, requestDto.getUserRequestSenderId());

        targetRoomFromDb.setName(requestDto.getName());
        targetRoomFromDb.setDescription(requestDto.getDescription());
        targetRoomFromDb.setPublic(requestDto.isPublic());

        if (requestDto.getLocation() != null) {
            FieldLocation editedLocation = fieldLocationMapper.requestDtoToFieldLocation(requestDto.getLocation());
            targetRoomFromDb.setFieldLocation(editedLocation);
        }
    }

    @Transactional
    public void updateNextMatchDate(final Long roomId, final Long adminId, final String dateString) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, adminId);

        DateFormat format = new SimpleDateFormat("MMMM dd yyyy'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        Date date;

        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zone);
        LocalDateTime nextMatchLocalDateTime = zonedDateTime.toLocalDateTime();

        targetRoomFromDb.setNextMatchDate(nextMatchLocalDateTime);
    }

    @Transactional
    public RoomNewUserResponseDto addUserToRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId());

        User userToAddFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.getUsersInRoom().add(userToAddFromDb);

        return roomMapper.roomToRoomNewUserResponseDto(targetRoomFromDb, userToAddFromDb);
    }

    @Transactional
    public void removeUserFromRoom(final RoomAddOrRemoveUserRequestDto requestDto) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(requestDto.getRoomId(), requestDto.getAdminId());

        User userToAddFromDb = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.getUsersInRoom().remove(userToAddFromDb);
    }

    private Room obtainRoomFromDbAndCheckAdminPermission(final Long roomId, final Long adminId) {
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

    public List<RoomSimpleDto> findRoomByNameOrLocation(String roomNameOrLocation) {
        List<Room> foundRooms = roomRepository.findByNameContainsIgnoreCaseOrFieldLocation_CityContainsIgnoreCaseOrFieldLocation_StreetContainsIgnoreCase(roomNameOrLocation, roomNameOrLocation, roomNameOrLocation)
                .stream()
                .filter(Room::isPublic)
                .collect(Collectors.toList());

        return roomMapper.roomCollectionToRoomSimpleDtoList(foundRooms, null);
    }
}
