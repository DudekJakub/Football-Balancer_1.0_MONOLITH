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
import com.dudek.footballbalancer.service.geocoding.GeocodingService;
import com.dudek.footballbalancer.validation.DateValidator;
import com.google.maps.model.LatLng;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final FieldLocationMapper fieldLocationMapper;
    private final PasswordEncryptor passwordEncryptor;
    private final GeocodingService geocodingService;

    @Autowired
    public RoomService(final RoomRepository roomRepository, final UserRepository userRepository, final RoomMapper roomMapper, final FieldLocationMapper fieldLocationMapper, final PasswordEncryptor passwordEncryptor, final GeocodingService geocodingService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
        this.fieldLocationMapper = fieldLocationMapper;
        this.passwordEncryptor = passwordEncryptor;
        this.geocodingService = geocodingService;
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

    public List<RoomSimpleDto> findRoomByNameOrLocation(String roomNameOrLocation) {
        List<Room> foundRooms = roomRepository.findByNameContainsIgnoreCaseOrFieldLocation_CityContainsIgnoreCaseOrFieldLocation_StreetContainsIgnoreCase(roomNameOrLocation, roomNameOrLocation, roomNameOrLocation)
                .stream()
                .filter(Room::isPublic)
                .collect(Collectors.toList());

        return roomMapper.roomCollectionToRoomSimpleDtoList(foundRooms, null);
    }

    public RoomEnteredResponseDto enterRoom(final RoomEnterRequestDto requestDto){
        final Room targetRoomFromDb = roomRepository.findByIdFetchPlayersUsersAdmins(requestDto.getRoomId())
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
            LatLng latLngFromRoomLocation = getLatLngFromRoomLocation(roomLocation);
            roomLocation.setLatitude(latLngFromRoomLocation.lat);
            roomLocation.setLongitude(latLngFromRoomLocation.lng);
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
            FieldLocation roomLocation = fieldLocationMapper.requestDtoToFieldLocation(requestDto.getLocation());
            LatLng latLngFromRoomLocation = getLatLngFromRoomLocation(roomLocation);
            roomLocation.setLatitude(latLngFromRoomLocation.lat);
            roomLocation.setLongitude(latLngFromRoomLocation.lng);
            targetRoomFromDb.setFieldLocation(roomLocation);
        }
    }

    @Transactional
    public void updateNextMatchDate(final Long roomId, final Long adminId, final String dateString) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, adminId);
        LocalDateTime nextMatchLocalDateTime = parseDateString(dateString);
        LocalDateTime nextMatchRegistrationStartDate = targetRoomFromDb.getNextMatchRegistrationStartDate();
        LocalDateTime nextMatchRegistrationEndDate = targetRoomFromDb.getNextMatchRegistrationEndDate();

        if (nextMatchRegistrationStartDate != null && nextMatchRegistrationEndDate != null) {
            boolean allDatesNotNullAndInOrder = DateValidator.allDatesNotNullAndInOrder(nextMatchRegistrationStartDate, nextMatchRegistrationEndDate, nextMatchLocalDateTime);
            if (allDatesNotNullAndInOrder) {
                targetRoomFromDb.setNextMatchDate(nextMatchLocalDateTime);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided date is in incorrect order (must be after registration start & end date) or is not present.");
            }
        } else {
            targetRoomFromDb.setNextMatchDate(nextMatchLocalDateTime);
        }
    }

    @Transactional
    public void updateNextMatchRegistrationStartDate(final Long roomId, final Long adminId, final String dateString) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, adminId);
        LocalDateTime nextMatchLocalDateTime = targetRoomFromDb.getNextMatchDate();
        LocalDateTime nextMatchRegistrationStartDate = parseDateString(dateString);
        LocalDateTime nextMatchRegistrationEndDate = targetRoomFromDb.getNextMatchRegistrationEndDate();

        boolean allDatesNotNullAndInOrder = DateValidator.allDatesNotNullAndInOrder(nextMatchRegistrationStartDate, nextMatchRegistrationEndDate, nextMatchLocalDateTime);

        if (allDatesNotNullAndInOrder) {
            targetRoomFromDb.setNextMatchRegistrationStartDate(nextMatchRegistrationStartDate);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided date is in incorrect order (must be before registration end date and before match date) or is not present.");
        }
    }

    @Transactional
    public void updateNextMatchRegistrationEndDate(final Long roomId, final Long adminId, final String dateString) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, adminId);
        LocalDateTime nextMatchLocalDateTime = targetRoomFromDb.getNextMatchDate();
        LocalDateTime nextMatchRegistrationStartDate = targetRoomFromDb.getNextMatchRegistrationStartDate();
        LocalDateTime nextMatchRegistrationEndDate = parseDateString(dateString);

        boolean allDatesNotNullAndInOrder = DateValidator.allDatesNotNullAndInOrder(nextMatchRegistrationStartDate, nextMatchRegistrationEndDate, nextMatchLocalDateTime);

        if (allDatesNotNullAndInOrder) {
            targetRoomFromDb.setNextMatchRegistrationEndDate(nextMatchRegistrationEndDate);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided date is in incorrect order (must be after registration start date and before match date) or is not present.");
        }
    }

    @Transactional
    public void updateNextMatchAllDates(Long roomId, Long adminId, RoomNewDatesRequestDto requestDto) {
        final Room targetRoomFromDb = obtainRoomFromDbAndCheckAdminPermission(roomId, adminId);
        LocalDateTime nextMatchLocalDateTime = parseDateString(requestDto.getNextMatchDate());
        LocalDateTime nextMatchRegistrationStartDate = parseDateString(requestDto.getNextMatchRegistrationStartDate());
        LocalDateTime nextMatchRegistrationEndDate = parseDateString(requestDto.getNextMatchRegistrationEndDate());

        boolean allDatesNotNullAndInOrder = DateValidator.allDatesNotNullAndInOrder(nextMatchRegistrationStartDate, nextMatchRegistrationEndDate, nextMatchLocalDateTime);

        if (allDatesNotNullAndInOrder) {
            targetRoomFromDb.setNextMatchDate(nextMatchLocalDateTime);
            targetRoomFromDb.setNextMatchRegistrationStartDate(nextMatchRegistrationStartDate);
            targetRoomFromDb.setNextMatchRegistrationEndDate(nextMatchRegistrationEndDate);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided dates are in incorrect order or are not present.");
        }
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

    private LocalDateTime parseDateString(final String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.GERMANY);

        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString, formatter);
            return offsetDateTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private LatLng getLatLngFromRoomLocation(final FieldLocation fieldLocation) {
        String address = fieldLocation.getStreet() + " " + fieldLocation.getNumber() + ", " + fieldLocation.getCity() + ", Poland";
        try {
            return geocodingService.getLatLng(address);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new LatLng(0,0);
        }
    }
}
