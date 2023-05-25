package com.dudek.footballbalancer.service.room;

import com.dudek.footballbalancer.config.security.PasswordEncryptor;
import com.dudek.footballbalancer.mapper.FieldLocationMapper;
import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.geocoding.GeocodingService;
import com.dudek.footballbalancer.service.util.DateFormatterUtil;
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
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.dudek.footballbalancer.service.util.RequestContextHolderUtil.*;

@Service
public class RoomBasicManagementService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomMapper roomMapper;
    private final FieldLocationMapper fieldLocationMapper;
    private final PasswordEncryptor passwordEncryptor;
    private final GeocodingService geocodingService;

    @Autowired
    public RoomBasicManagementService(final RoomRepository roomRepository, final UserRepository userRepository, final RoomMapper roomMapper,
                                      final FieldLocationMapper fieldLocationMapper, final PasswordEncryptor passwordEncryptor, final GeocodingService geocodingService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomMapper = roomMapper;
        this.fieldLocationMapper = fieldLocationMapper;
        this.passwordEncryptor = passwordEncryptor;
        this.geocodingService = geocodingService;
    }

    public List<RoomSimpleDto> findPaginatedWithoutUserOwned(int pageNumber, int pageSize, Sort.Direction sortDirection, String sortField, boolean fetchPublic, Long userId) {
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

    public List<RoomSimpleDto> findRoomByNameOrLocation(final String roomNameOrLocation, final Long userId) {
        List<Room> foundRooms = roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(roomNameOrLocation, roomNameOrLocation, roomNameOrLocation)
                .stream()
                .filter(Room::isPublic)
                .collect(Collectors.toList());

        return roomMapper.roomCollectionToRoomSimpleDtoList(foundRooms, userId);
    }

    public RoomEnteredResponseDto enterRoom(final RoomEnterRequestDto requestDto) {
        final Room targetRoomFromDb = roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(requestDto.getRoomId())
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
                .playersInRoom(Set.of())
                .build();

        if (requestDto.getLocation() != null) {
            FieldLocation roomLocation = prepareFieldLocationFromRequest(requestDto.getLocation());
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
    public RoomEditResponseDto updateRoom(final Long roomId, final RoomEditRequestDto requestDto) {
        final Room targetRoomFromDb = roomRepository.findById(roomId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        targetRoomFromDb.setName(requestDto.getName());
        targetRoomFromDb.setDescription(requestDto.getDescription());
        targetRoomFromDb.setPublic(requestDto.isPublic());

        if (requestDto.getLocation() != null) {
            FieldLocation roomLocation = prepareFieldLocationFromRequest(requestDto.getLocation());
            targetRoomFromDb.setFieldLocation(roomLocation);
            setContextAttribute(TARGET_ROOM, targetRoomFromDb);
            return new RoomEditResponseDto(targetRoomFromDb, roomLocation);
        }
        setContextAttribute(TARGET_ROOM, targetRoomFromDb);
        return new RoomEditResponseDto(targetRoomFromDb);
    }

    @Transactional
    public void updateNextMatchDates(Long roomId, RoomNewDatesRequestDto requestDto) {
        final Room targetRoomFromDb = roomRepository.findById(roomId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime nextMatchLocalDateTime = DateFormatterUtil.parseDateString(requestDto.getNextMatchDate());
        LocalDateTime nextMatchRegistrationStartDate = DateFormatterUtil.parseDateString(requestDto.getNextMatchRegistrationStartDate());
        LocalDateTime nextMatchRegistrationEndDate = DateFormatterUtil.parseDateString(requestDto.getNextMatchRegistrationEndDate());

        boolean allDatesNotNullAndInOrder = DateValidator.allDatesNotNullAndInOrder(nextMatchRegistrationStartDate, nextMatchRegistrationEndDate, nextMatchLocalDateTime);

        if (allDatesNotNullAndInOrder) {
            targetRoomFromDb.setNextMatchDate(nextMatchLocalDateTime);
            targetRoomFromDb.setNextMatchRegistrationStartDate(nextMatchRegistrationStartDate);
            targetRoomFromDb.setNextMatchRegistrationEndDate(nextMatchRegistrationEndDate);
            setContextAttribute(TARGET_ROOM, targetRoomFromDb);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided dates are in incorrect order or are not present.");
        }
    }

    FieldLocation prepareFieldLocationFromRequest(final FieldLocationRequestDto requestDto) {
        FieldLocation roomLocation = fieldLocationMapper.requestDtoToFieldLocation(requestDto);
        String address = roomLocation.getStreet() + " " + roomLocation.getNumber() + ", " + roomLocation.getCity() + ", Poland";
        LatLng latLngFromRoomLocation;
        try {
            latLngFromRoomLocation = geocodingService.getLatLng(address);
        } catch (Exception e) {
            latLngFromRoomLocation = new LatLng(0,0);
        }
        roomLocation.setLatitude(latLngFromRoomLocation.lat);
        roomLocation.setLongitude(latLngFromRoomLocation.lng);
        return roomLocation;
    }
}
