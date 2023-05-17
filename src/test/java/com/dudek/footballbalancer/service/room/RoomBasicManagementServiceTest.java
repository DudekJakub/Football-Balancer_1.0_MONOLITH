package com.dudek.footballbalancer.service.room;

import com.dudek.footballbalancer.config.security.PasswordEncryptor;
import com.dudek.footballbalancer.mapper.FieldLocationMapper;
import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.model.dto.user.UserSimpleDtoForRoom;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.geocoding.GeocodingService;
import com.google.maps.model.LatLng;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomBasicManagementServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private FieldLocationMapper fieldLocationMapper;

    @Mock
    private PasswordEncryptor passwordEncryptor;

    @Mock
    private GeocodingService geocodingService;

    @InjectMocks
    private RoomBasicManagementService roomService;

    @Test
    void itShouldFindPaginated() {
        //given
        int pageNumber = 1;
        int pageSize = 10;
        Sort.Direction sortDirection = Sort.Direction.ASC;
        String sortField = "id";
        boolean fetchPublic = true;
        Long userId = 0L;

        List<Room> rooms = IntStream.range(1, 21)
                .mapToObj(i -> {
                    Room room = new Room();
                    room.setName("TestRoom" + i);
                    room.setPassword("TestPassword" + i);
                    room.setId((long) i);
                    room.setPublic(true);
                    return room;
                })
                .collect(Collectors.toList());

        List<RoomSimpleDto> expected = IntStream.range(1, 21)
                .mapToObj(i -> {
                    RoomSimpleDto simpleDto = new RoomSimpleDto();
                    simpleDto.setName("TestRoom" + i);
                    simpleDto.setId((long) i);
                    simpleDto.setPublic(true);
                    return simpleDto;
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortField));
        Page<Room> page = new PageImpl<>(rooms, pageable, rooms.size());

        when(roomRepository.findPaginatedFetchUsersInRoomAndLocation(pageable, fetchPublic, userId)).thenReturn(page);
        when(roomMapper.roomCollectionToRoomSimpleDtoList(rooms, userId)).thenReturn(expected);

        //when
        List<RoomSimpleDto> actual = roomService.findPaginatedWithoutUserOwned(pageNumber, pageSize, sortDirection, sortField, fetchPublic, userId);

        //then
        assertEquals(expected.size(), actual.size());
        assertAll(() -> {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).getId(), actual.get(i).getId());
                assertEquals(expected.get(i).getName(), actual.get(i).getName());
                assertEquals(expected.get(i).isPublic(), actual.get(i).isPublic());
            }
        });
    }

    @Test
    void itShouldFindPaginatedByUserId() {
        //given
        int pageNumber = 1;
        int pageSize = 10;
        Sort.Direction sortDirection = Sort.Direction.ASC;
        String sortField = "id";
        boolean fetchPublic = true;
        Long userId = 1L;
        User user = User.builder()
                .username("testUsername")
                .firstName("Jakub")
                .lastName("Dudek")
                .id(userId)
                .build();

        List<Room> rooms = IntStream.range(1, 21)
                .mapToObj(i -> {
                    Room room = new Room();
                    room.setName("TestRoom" + i);
                    room.setPassword("TestPassword" + i);
                    room.setId((long) i);
                    room.setPublic(true);
                    room.setUsersInRoom(Set.of(user));
                    return room;
                })
                .collect(Collectors.toList());

        List<RoomSimpleDto> expected = IntStream.range(1, 21)
                .mapToObj(i -> {
                    RoomSimpleDto simpleDto = new RoomSimpleDto();
                    simpleDto.setName("TestRoom" + i);
                    simpleDto.setId((long) i);
                    simpleDto.setPublic(true);
                    simpleDto.setUserInRoom(true);
                    return simpleDto;
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortField));
        Page<Room> page = new PageImpl<>(rooms, pageable, rooms.size());

        when(roomRepository.findPaginatedFetchUsersInRoomAndLocation(pageable, fetchPublic, userId)).thenReturn(page);
        when(roomMapper.roomCollectionToRoomSimpleDtoList(rooms, userId)).thenReturn(expected);

        //when
        List<RoomSimpleDto> actual = roomService.findPaginatedWithoutUserOwned(pageNumber, pageSize, sortDirection, sortField, fetchPublic, userId);

        //then
        assertEquals(expected.size(), actual.size());
        assertAll(() -> {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).getId(), actual.get(i).getId());
                assertEquals(expected.get(i).getName(), actual.get(i).getName());
                assertEquals(expected.get(i).isPublic(), actual.get(i).isPublic());
                assertEquals(expected.get(i).getUsersInRoomQuantity(), actual.get(i).getUsersInRoomQuantity());
                assertEquals(expected.get(i).isUserInRoom(), actual.get(i).isUserInRoom());
            }
        });
    }

    @Test
    void itShouldFindRoomByNameOrLocation() {
        //given
        String searchQuery = "TestRoom1";
        Long userId = 1L;

        Room room = new Room();
        room.setName("TestRoom1");
        room.setId(1L);

        RoomSimpleDto simpleDto = new RoomSimpleDto();
        simpleDto.setName(room.getName());
        simpleDto.setId(room.getId());

        List<Room> rooms = List.of(room);
        List<RoomSimpleDto> expected = List.of(simpleDto);

        when(roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(searchQuery, searchQuery, searchQuery))
                .thenReturn(rooms);
        when(roomMapper.roomCollectionToRoomSimpleDtoList(rooms, userId))
                .thenReturn(expected);

        //when
        List<RoomSimpleDto> actual = roomService.findRoomByNameOrLocation(searchQuery, userId);

        //then
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getName(), actual.get(0).getName());
        assertEquals(expected.get(0).getCity(), actual.get(0).getCity());
        assertEquals(expected.get(0).isPublic(), actual.get(0).isPublic());
    }

    @Test
    void itShouldEnterRoomSuccessfullyWhenUserIsNotMember() {
        //given
        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .roomId(1L)
                .password("testPassword1")
                .userId(1L)
                .build();

        Room room = new Room();
        room.setId(1L);
        room.setName("TestRoom1");
        room.setPassword("testPassword1");
        room.setUsersInRoom(Set.of());

        RoomEnteredResponseDto enteredResponseDto = RoomEnteredResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .users(List.of())
                .build();

        when(roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(1L)).thenReturn(Optional.of(room));
        when(passwordEncryptor.checkPasswordMatch(room.getPassword(), requestDto.getPassword())).thenReturn(true);
        when(roomMapper.roomToRoomEnteredResponseDto(room)).thenReturn(enteredResponseDto);

        //when
        RoomEnteredResponseDto responseDto = roomService.enterRoom(requestDto);

        //then
        assertDoesNotThrow(() -> {
            roomService.enterRoom(requestDto);
        });
        assertEquals(room.getId(), responseDto.getId());
        assertEquals(room.getName(), responseDto.getName());
        assertEquals(room.getUsersInRoom().size(), responseDto.getUsers().size());
    }

    @Test
    void itShouldEnterRoomUnsuccessfullyWhenUserIsNotMember() {
        //given
        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .roomId(1L)
                .password("testPassword2")
                .userId(1L)
                .build();

        Room room = new Room();
        room.setId(1L);
        room.setName("TestRoom1");
        room.setPassword("testPassword1");
        room.setUsersInRoom(Set.of());

        when(roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(1L)).thenReturn(Optional.of(room));
        when(passwordEncryptor.checkPasswordMatch(room.getPassword(), requestDto.getPassword())).thenReturn(false);

        //then
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            roomService.enterRoom(requestDto);
        });

        assertTrue(exception.getMessage().contains("Wrong password!"));
    }

    @Test
    void itShouldEnterRoomSuccessfullyWhenUserIsMember() {
        //given
        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .roomId(1L)
                .password("testPassword2")
                .userId(1L)
                .build();

        User user = new User();
        user.setId(1L);

        UserSimpleDtoForRoom userSimpleDto = new UserSimpleDtoForRoom();
        userSimpleDto.setId(user.getId());

        Room room = new Room();
        room.setId(1L);
        room.setName("TestRoom1");
        room.setPassword("testPassword1");
        room.setUsersInRoom(Set.of(user));

        RoomEnteredResponseDto enteredResponseDto = RoomEnteredResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .users(List.of(userSimpleDto))
                .build();

        when(roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(1L)).thenReturn(Optional.of(room));
        when(roomMapper.roomToRoomEnteredResponseDto(room)).thenReturn(enteredResponseDto);

        //when
        RoomEnteredResponseDto responseDto = roomService.enterRoom(requestDto);

        //then
        assertDoesNotThrow(() -> {
            roomService.enterRoom(requestDto);
        });

        verifyNoInteractions(passwordEncryptor);

        assertEquals(room.getId(), responseDto.getId());
        assertEquals(room.getName(), responseDto.getName());
        assertEquals(room.getUsersInRoom().size(), responseDto.getUsers().size());
    }

    @Test
    void itShouldCreateRoomWithoutLocation() {
        //given
        User user = User.builder()
                .id(1L)
                .username("TestUsername")
                .lastName("Jakub")
                .lastName("Dudek")
                .build();

        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .userRequestSenderId(user.getId())
                .isPublic(true)
                .roomName("TestRoomName")
                .roomPassword("TestRoomPassword")
                .build();

        Room newRoom = new Room();
        newRoom.setId(1L);
        newRoom.setPassword(requestDto.getRoomPassword());
        newRoom.setPublic(true);
        newRoom.setAdminsInRoom(Set.of(user));
        newRoom.setUsersInRoom(Set.of(user));
        newRoom.setFieldLocation(new FieldLocation());

        UserSimpleDtoForRoom userSimpleDto = new UserSimpleDtoForRoom();
        userSimpleDto.setId(user.getId());

        RoomNewResponseDto responseDto = RoomNewResponseDto.builder()
                .id(newRoom.getId())
                .location(new FieldLocationSimpleDto())
                .admins(List.of(userSimpleDto))
                .users(List.of(userSimpleDto))
                .build();

        when(userRepository.findById(requestDto.getUserRequestSenderId())).thenReturn(Optional.of(user));
        when(passwordEncryptor.encrypt(requestDto.getRoomPassword())).thenReturn(Optional.of("TestRoomPassword"));
        when(roomRepository.save(any(Room.class))).thenReturn(newRoom);
        when(roomMapper.roomToRoomNewResponseDto(newRoom)).thenReturn(responseDto);

        //when
        RoomNewResponseDto actualResponse = roomService.createRoom(requestDto);

        //then
        verify(userRepository).findById(requestDto.getUserRequestSenderId());
        verify(passwordEncryptor).encrypt(requestDto.getRoomPassword());
        verify(roomRepository).save(any(Room.class));
        verify(roomMapper).roomToRoomNewResponseDto(newRoom);
        verifyNoInteractions(fieldLocationMapper);
        verifyNoMoreInteractions(userRepository, passwordEncryptor, roomRepository, roomMapper);

        assertDoesNotThrow(() -> {
            roomService.createRoom(requestDto);
        });

        assertEquals(user.getId(), actualResponse.getUsers().get(0).getId());
        assertEquals(user.getId(), actualResponse.getAdmins().get(0).getId());
    }

    @Test
    void itShouldCreateRoomWithLocation() throws Exception {
        //given
        RoomNewRequestDto requestDto = new RoomNewRequestDto();
        requestDto.setRoomName("Test Room");
        requestDto.setRoomPassword("testpassword");
        requestDto.setPublic(true);
        requestDto.setUserRequestSenderId(1L);

        FieldLocationRequestDto locationDto = new FieldLocationRequestDto();
        locationDto.setCity("Test City");
        locationDto.setStreet("Test Street");
        locationDto.setNumber(1);
        requestDto.setLocation(locationDto);

        User requestSender = new User();
        requestSender.setId(1L);

        String encryptedPassword = "encryptedpassword";

        FieldLocation fieldLocation = FieldLocation.builder()
                .city(locationDto.getCity())
                .street(locationDto.getStreet())
                .number(locationDto.getNumber())
                .build();

        Room newRoom = Room.builder()
                .name(requestDto.getRoomName())
                .password(encryptedPassword)
                .isPublic(requestDto.isPublic())
                .adminsInRoom(Set.of(requestSender))
                .usersInRoom(Set.of(requestSender))
                .fieldLocation(fieldLocation)
                .build();

        RoomNewResponseDto expectedResponse = new RoomNewResponseDto();

        when(userRepository.findById(requestDto.getUserRequestSenderId())).thenReturn(Optional.of(requestSender));
        when(passwordEncryptor.encrypt(requestDto.getRoomPassword())).thenReturn(Optional.of(encryptedPassword));
        when(fieldLocationMapper.requestDtoToFieldLocation(locationDto)).thenReturn(fieldLocation);
        when(geocodingService.getLatLng(anyString())).thenReturn(new LatLng(0, 0));
        when(roomMapper.roomToRoomNewResponseDto(newRoom)).thenReturn(expectedResponse);
        when(roomRepository.save(any(Room.class))).thenReturn(newRoom);

        //when
        RoomNewResponseDto actualResponse = roomService.createRoom(requestDto);

        //then
        verify(userRepository).findById(requestDto.getUserRequestSenderId());
        verify(passwordEncryptor).encrypt(requestDto.getRoomPassword());
        verify(fieldLocationMapper).requestDtoToFieldLocation(locationDto);
        verify(roomRepository).save(any(Room.class));
        verify(roomMapper).roomToRoomNewResponseDto(newRoom);
        verifyNoMoreInteractions(userRepository, passwordEncryptor, fieldLocationMapper, roomRepository, roomMapper);

        assertDoesNotThrow(() -> {
            roomService.createRoom(requestDto);
        });

        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    void itShouldUpdateRoomWithLocation() throws Exception {
        //given
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .name("TestRoom_2")
                .isPublic(false)
                .description("TestDescription")
                .location(
                        FieldLocationRequestDto.builder()
                                .city("Poznan")
                                .street("Lozowa")
                                .build()
                ).build();

        Room roomToUpdate = Room.builder()
                .name("TestRoom")
                .description("TestDescription")
                .password("encryptedPassword")
                .isPublic(true)
                .fieldLocation(new FieldLocation())
                .build();
        roomToUpdate.setId(1L);

        FieldLocation updatedLocation = FieldLocation.builder()
                .city("Poznan")
                .street("Lozowa")
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomToUpdate));
        when(fieldLocationMapper.requestDtoToFieldLocation(any(FieldLocationRequestDto.class))).thenReturn(updatedLocation);
        when(geocodingService.getLatLng(anyString())).thenReturn(new LatLng(10,20));

        //when
        RoomEditResponseDto responseDto = roomService.updateRoom(1L, requestDto);

        //then
        verify(roomRepository).findById(1L);
        verify(fieldLocationMapper).requestDtoToFieldLocation(any(FieldLocationRequestDto.class));
        verify(geocodingService).getLatLng(anyString());
        verifyNoMoreInteractions(roomRepository, fieldLocationMapper, geocodingService);

        assertDoesNotThrow(() -> {
            roomService.updateRoom(1L, requestDto);
        });

        assertEquals(requestDto.getName(), responseDto.getName());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertNotNull(requestDto.getLocation());
        assertEquals(requestDto.getLocation().getCity(), responseDto.getLocation().getCity());
        assertEquals(requestDto.getLocation().getStreet(), responseDto.getLocation().getStreet());
        assertEquals(10, responseDto.getLocation().getLatitude());
        assertEquals(20, responseDto.getLocation().getLongitude());
    }

    @Test
    void itShouldUpdateRoomWithoutLocation() {
        //given
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .name("TestRoom_2")
                .isPublic(false)
                .description("TestDescription")
                .build();

        Room roomToUpdate = Room.builder()
                .name("TestRoom")
                .description("TestDescription")
                .password("encryptedPassword")
                .isPublic(true)
                .build();
        roomToUpdate.setId(1L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomToUpdate));

        //when
        RoomEditResponseDto responseDto = roomService.updateRoom(1L, requestDto);

        //then
        verify(roomRepository).findById(1L);
        verifyNoInteractions(fieldLocationMapper, geocodingService);
        verifyNoMoreInteractions(roomRepository);

        assertDoesNotThrow(() -> {
            roomService.updateRoom(1L, requestDto);
        });

        assertEquals(requestDto.getName(), responseDto.getName());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertNull(responseDto.getLocation());
    }

    @Test
    void itShouldUpdateNextMatchDatesSuccessfullyWithCorrectDatesProvided() {
        //given
        long roomId = 1L;
        Room room = new Room();
        room.setId(roomId);
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in correct order
                .roomAdminId(1L)
                .nextMatchDate(now.plusDays(5L).format(formatter))
                .nextMatchRegistrationStartDate(now.format(formatter))
                .nextMatchRegistrationEndDate(now.plusDays(4L).format(formatter))
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        //then
        assertDoesNotThrow(() -> {
            roomService.updateNextMatchDates(roomId, requestDto);
        });

        assertNotNull(room.getNextMatchDate());
        assertNotNull(room.getNextMatchRegistrationStartDate());
        assertNotNull(room.getNextMatchRegistrationEndDate());

        verify(roomRepository).findById(roomId);
        verifyNoMoreInteractions(roomRepository);
        verifyNoInteractions(roomMapper, fieldLocationMapper, geocodingService, userRepository, passwordEncryptor);
    }

    @Test
    void itShouldUpdateNextMatchDatesUnsuccessfullyWithIncorrectDatesProvided() {
        //given
        long roomId = 1L;
        Room room = new Room();
        room.setId(roomId);
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in incorrect order
                .roomAdminId(1L)
                .nextMatchDate(now.plusDays(5L).format(formatter))
                .nextMatchRegistrationStartDate(now.plusDays(6L).format(formatter))
                .nextMatchRegistrationEndDate(now.plusDays(4L).format(formatter))
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        //then
        Exception exception = assertThrows(ResponseStatusException.class, () -> roomService.updateNextMatchDates(roomId, requestDto));

        verify(roomRepository).findById(roomId);
        verifyNoMoreInteractions(roomRepository);
        verifyNoInteractions(roomMapper, fieldLocationMapper, geocodingService, userRepository, passwordEncryptor);

        assertTrue(exception.getMessage().contains("Provided dates are in incorrect order or are not present."));
        assertEquals(ResponseStatusException.class, exception.getClass());
        assertNull(room.getNextMatchDate());
        assertNull(room.getNextMatchRegistrationStartDate());
        assertNull(room.getNextMatchRegistrationEndDate());
    }
}