package com.dudek.footballbalancer.integration;

import com.dudek.footballbalancer.config.security.PasswordEncryptor;
import com.dudek.footballbalancer.mapper.FieldLocationMapper;
import com.dudek.footballbalancer.mapper.RoomMapper;
import com.dudek.footballbalancer.model.Role;
import com.dudek.footballbalancer.model.SexStatus;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationSimpleDto;
import com.dudek.footballbalancer.model.dto.room.*;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.dudek.footballbalancer.service.geocoding.GeocodingService;
import com.dudek.footballbalancer.service.room.RoomBasicManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
public class RoomBasicManagementServiceIntegrationTests {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private FieldLocationMapper fieldLocationMapper;

    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private RoomBasicManagementService roomService;

    private static User user;
    private static final List<Room> rooms = new ArrayList<>();

    private void setInitialData() {
        rooms.clear();
        for (int i = 0; i < 6; i++) {
            Room room = Room.builder()
                    .name("TestRoomName" + (i + 1))
                    .description("TestRoomDescription" + (i + 1))
                    .password("TestRoomPassword" + (i + 1))
                    .isPublic(true)
                    .fieldLocation(new FieldLocation())
                    .usersInRoom(Set.of())
                    .build();
            rooms.add(room);
        }
        user = User.builder()
                .username("TestUsername")
                .password("TestPassword")
                .lastName("TestLastname")
                .firstName("TestFirstname")
                .sex(SexStatus.MALE)
                .role(Role.USER)
                .build();
    }

    @BeforeEach
    void setup() {
        roomRepository.deleteAll();
        userRepository.deleteAll();
        setInitialData();
        roomRepository.saveAll(rooms);
        userRepository.save(user);
    }

    @Test
    void itShouldFindPaginatedAndPublicWithoutUserOwned() {
        //given
        long userId = user.getId();
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findPaginatedWithoutUserOwned(0, 10, Sort.Direction.ASC, "id", true, userId);

        //then
        assertEquals(6, rooms.size());
        assertEquals(5, resultList.size());


        for (int i = 0; i < resultList.size() - 1; i++) {
            Long currentRoomId = resultList.get(i).getId();
            boolean currentRoomAccess = resultList.get(i).isPublic();
            assertEquals(1, resultList.get(i + 1).getId() - currentRoomId); //checking if sorted by ID field with ASC. direction
            assertTrue(currentRoomAccess);
        }
    }

    @Test
    void itShouldFindPaginatedAndPrivateWithoutUserOwned() {
        //given
        long userId = user.getId();
        roomRepository.findAll().forEach(r -> {
            if (Objects.equals(r.getId(), rooms.get(0).getId())) {
                r.setUsersInRoom(Set.of(user));
            }
            r.setPublic(false);
            roomRepository.save(r);
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findPaginatedWithoutUserOwned(0, 10, Sort.Direction.ASC, "id", false, userId);

        //then
        assertEquals(6, rooms.size());
        assertEquals(5, resultList.size());

        for (int i = 0; i < resultList.size() - 1; i++) {
            Long currentRoomId = resultList.get(i).getId();
            boolean currentRoomAccess = resultList.get(i).isPublic();
            assertEquals(1, resultList.get(i + 1).getId() - currentRoomId); //checking if sorted by ID field with ASC. direction
            assertFalse(currentRoomAccess);
        }
    }

    @Test
    void itShouldFindOnePaginatedRoomsByUserId() {
        //given
        long userId = user.getId();
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findPaginatedByUserId(0, 10, Sort.Direction.ASC, "id", userId);

        //then
        assertEquals(6, rooms.size());
        assertEquals(1, resultList.size());
        assertEquals(1, resultList.get(0).getUsersInRoomQuantity());
    }

    @Test
    void itShouldFindFourPaginatedRoomsByUserId() {
        //given
        long userId = user.getId();
        AtomicInteger counter = new AtomicInteger();
        roomRepository.findAll().forEach(r -> {
            if (counter.get() < 4) {
                r.setUsersInRoom(Set.of(user));
                roomRepository.save(r);
                counter.getAndIncrement();
            }
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findPaginatedByUserId(0, 10, Sort.Direction.ASC, "id", userId);

        //then
        assertEquals(6, rooms.size());
        assertEquals(4, resultList.size());
        for (RoomSimpleDto resultDto : resultList) {
            assertEquals(1, resultDto.getUsersInRoomQuantity());
        }
    }

    @Test
    void itShouldFindOneRoomByGivenName() {
        //given
        String roomName = "TestRoomName1";

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(1, resultList.size());
        assertEquals(roomName, resultList.get(0).getName());
    }

    @Test
    void itShouldFindFourRoomsByGivenName() {
        //given
        String roomName = "TestRoomName1";
        roomRepository.findAll().forEach(r -> {
            if (r.getId() <= 4) {
                r.setName(roomName);
                roomRepository.save(r);
            }
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(4, resultList.size());
        for (RoomSimpleDto resultDto : resultList) {
            assertEquals(roomName, resultDto.getName());
        }
    }

    @Test
    void itShouldFindOneRoomByFieldLocationCity() {
        //given
        String roomCityName = "Poznan";
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setFieldLocation(FieldLocation.builder().city(roomCityName).build());
            roomRepository.save(r);
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomCityName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(1, resultList.size());
        assertEquals(roomCityName, resultList.get(0).getCity());
    }

    @Test
    void itShouldFindFourRoomsByFieldLocationCity() {
        //given
        String roomCityName = "Poznan";
        AtomicInteger counter = new AtomicInteger();
        roomRepository.findAll().forEach(r -> {
            if (counter.get() < 4) {
                r.setFieldLocation(FieldLocation.builder().city(roomCityName).build());
                roomRepository.save(r);
                counter.getAndIncrement();
            }
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomCityName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(4, resultList.size());
        for (RoomSimpleDto resultDto : resultList) {
            assertEquals(roomCityName, resultDto.getCity());
        }
    }

    @Test
    void itShouldFindOneRoomByFieldLocationStreet() {
        //given
        String roomStreetName = "AwesomeStreet";
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setFieldLocation(FieldLocation.builder().street(roomStreetName).build());
            roomRepository.save(r);
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomStreetName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(1, resultList.size());
    }

    @Test
    void itShouldFindFourRoomsByFieldLocationStreet() {
        //given
        String roomStreetName = "AwesomeStreet";
        AtomicInteger counter = new AtomicInteger();
        roomRepository.findAll().forEach(r -> {
            if (counter.get() < 4) {
                r.setFieldLocation(FieldLocation.builder().street(roomStreetName).build());
                roomRepository.save(r);
                counter.getAndIncrement();
            }
        });

        //when
        List<RoomSimpleDto> resultList = roomService.findRoomByNameOrLocation(roomStreetName, 0L);

        //then
        assertEquals(6, rooms.size());
        assertEquals(4, resultList.size());
    }

    @Test
    void itShouldEnterRoomSuccessfullyWithProvidedCorrectPassword() {
        //given
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            passwordEncryptor.encrypt(r.getPassword()).ifPresent(r::setPassword);
            roomRepository.save(r);
        });
        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .roomId(rooms.get(0).getId())
                .userId(user.getId())
                .password("TestRoomPassword1")
                .build();

        //when
        RoomEnteredResponseDto resultDto = roomService.enterRoom(requestDto);

        //then
        assertNotNull(resultDto);
        assertEquals(rooms.get(0).getId(), resultDto.getId());
        assertEquals("TestRoomName1", resultDto.getName());
        assertEquals("TestRoomDescription1", resultDto.getDescription());
    }

    @Test
    void itShouldEnterRoomUnsuccessfullyWithProvidedWrongPassword() {
        //given
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            passwordEncryptor.encrypt(r.getPassword()).ifPresent(r::setPassword);
            roomRepository.save(r);
        });
        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .roomId(rooms.get(0).getId())
                .userId(user.getId())
                .password("TestRoomPassword10000")
                .build();

        //when
        Exception expectedException = assertThrows(ResponseStatusException.class, () -> roomService.enterRoom(requestDto));

        //then
        assertNotNull(expectedException);
        assertEquals(ResponseStatusException.class, expectedException.getClass());
        assertTrue(expectedException.getMessage().contains("Wrong password"));
    }

    @Test
    void itShouldCreateNewPublicRoomWithoutLocation() {
        //given
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestNewRoomName")
                .roomPassword("TestNewRoomPassword")
                .isPublic(true)
                .userRequestSenderId(user.getId())
                .build();

        //when
        RoomNewResponseDto resultDto = roomService.createRoom(requestDto);

        //then
        long resultRoomId = resultDto.getId();
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(resultRoomId).ifPresent(r -> {

            assertEquals(requestDto.getRoomName(), r.getName());
            assertNull(requestDto.getLocation());
            assertEquals("N/A", r.getFieldLocation().getCity());
            assertEquals("N/A", r.getFieldLocation().getStreet());
            assertEquals("00-000", r.getFieldLocation().getZipCode());
            assertEquals(0, r.getFieldLocation().getNumber());
            assertEquals(0, r.getFieldLocation().getLatitude());
            assertEquals(0, r.getFieldLocation().getLongitude());
            assertTrue(passwordEncryptor.checkPasswordMatch(r.getPassword(), requestDto.getRoomPassword()));
            assertTrue(r.isPublic());
            assertEquals(1, r.getUsersInRoom().size());
            assertEquals(1, r.getAdminsInRoom().size());
            assertTrue(r.getUsersInRoom().stream().mapToLong(User::getId).allMatch(value -> value == user.getId()));
            assertTrue(r.getAdminsInRoom().stream().mapToLong(User::getId).allMatch(value -> value == user.getId()));
        });
        assertNotNull(resultDto);
        assertNotNull(resultDto.getLocation());
        roomRepository.findAll()
                .stream()
                .mapToLong(Room::getId)
                .max()
                .ifPresent(id -> assertEquals(id, resultRoomId));
        assertTrue(roomRepository.existsById(resultRoomId));
        assertEquals(requestDto.getRoomName(), resultDto.getName());
        assertEquals("N/A", resultDto.getLocation().getCity());
        assertEquals("N/A", resultDto.getLocation().getStreet());
        assertEquals("00-000", resultDto.getLocation().getZipCode());
        assertEquals(0, resultDto.getLocation().getNumber());
        assertTrue(resultRoomId > 0);
        assertTrue(resultDto.isPublic());
        assertNull(resultDto.getDescription());
    }

    @Test
    void itShouldCreateNewPublicRoomWithLocation() {
        //given
        FieldLocationRequestDto locationRequestDto = FieldLocationRequestDto.builder()
                .city("Poznan")
                .street("AwesomeStreet")
                .zipCode("60-688")
                .number(10)
                .build();
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestNewRoomName")
                .roomPassword("TestNewRoomPassword")
                .isPublic(true)
                .userRequestSenderId(user.getId())
                .location(locationRequestDto)
                .build();
        long currentRoomsQuantity = roomRepository.count();

        //when
        RoomNewResponseDto resultDto = roomService.createRoom(requestDto);

        //then
        long resultRoomId = resultDto.getId();
        FieldLocationSimpleDto resultLocation = resultDto.getLocation();
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(resultRoomId).ifPresent(r -> {
            FieldLocationRequestDto locationRequest = requestDto.getLocation();

            assertEquals(requestDto.getRoomName(), r.getName());
            assertNotNull(locationRequest);
            assertEquals(locationRequest.getCity(), r.getFieldLocation().getCity());
            assertEquals(locationRequest.getStreet(), r.getFieldLocation().getStreet());
            assertEquals(locationRequest.getZipCode(), r.getFieldLocation().getZipCode());
            assertEquals(locationRequest.getNumber(), r.getFieldLocation().getNumber());
            assertTrue(r.getFieldLocation().getLatitude() > 0);
            assertTrue(r.getFieldLocation().getLongitude() > 0);
            assertTrue(passwordEncryptor.checkPasswordMatch(r.getPassword(), requestDto.getRoomPassword()));
            assertTrue(r.isPublic());
            assertEquals(1, r.getUsersInRoom().size());
            assertEquals(1, r.getAdminsInRoom().size());
            assertTrue(r.getUsersInRoom().stream().mapToLong(User::getId).allMatch(value -> value == user.getId()));
            assertTrue(r.getAdminsInRoom().stream().mapToLong(User::getId).allMatch(value -> value == user.getId()));
        });
        assertNotNull(resultDto);
        assertNotNull(resultDto.getLocation());
        roomRepository.findAll()
                .stream()
                .mapToLong(Room::getId)
                .max()
                .ifPresent(id -> assertEquals(id, resultRoomId));
        assertTrue(roomRepository.existsById(resultRoomId));
        assertEquals(requestDto.getRoomName(), resultDto.getName());
        assertEquals(locationRequestDto.getCity(), resultLocation.getCity());
        assertEquals(locationRequestDto.getStreet(), resultLocation.getStreet());
        assertEquals(locationRequestDto.getZipCode(), resultLocation.getZipCode());
        assertEquals(locationRequestDto.getNumber(), resultLocation.getNumber());
        assertTrue(resultLocation.getLatitude() > 0);
        assertTrue(resultLocation.getLongitude() > 0);
        assertTrue(resultRoomId > 0);
        assertTrue(resultDto.isPublic());
        assertNull(resultDto.getDescription());
    }

    @Test
    void itShouldUpdateRoomWithoutLocation() {
        //given
        String currentRoomLocationCity = "Poznan";
        String currentRoomLocationStreet = "AwesomeStreet";
        String currentRoomLocationZipCode = "60-688";
        int currentRoomLocationNumber = 10;
        double currentRoomLocationLatitude = 15;
        double currentRoomLocationLongitude = 15;
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(rooms.get(0).getId()).ifPresent(r -> {
            r.getFieldLocation().setCity(currentRoomLocationCity);
            r.getFieldLocation().setStreet(currentRoomLocationStreet);
            r.getFieldLocation().setZipCode(currentRoomLocationZipCode);
            r.getFieldLocation().setNumber(currentRoomLocationNumber);
            r.getFieldLocation().setLatitude(currentRoomLocationLatitude);
            r.getFieldLocation().setLongitude(currentRoomLocationLongitude);
            roomRepository.save(r);
        });
        long roomToUpdateId = rooms.get(0).getId();;
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .name("NewRoomName")
                .description("NewRoomDescription")
                .isPublic(true)
                .roomAdminId(user.getId())
                .build();

        //when
        RoomEditResponseDto resultDto = roomService.updateRoom(roomToUpdateId, requestDto);

        //then
        assertNotNull(requestDto);
        assertNull(requestDto.getLocation());
        assertNull(resultDto.getLocation());
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(roomToUpdateId).ifPresent(r -> {
            FieldLocation roomLocation = r.getFieldLocation();

            assertEquals(requestDto.getName(), r.getName());
            assertEquals(requestDto.getDescription(), r.getDescription());
            assertEquals(requestDto.isPublic(), r.isPublic());
            assertEquals(currentRoomLocationCity, roomLocation.getCity());
            assertEquals(currentRoomLocationStreet, roomLocation.getStreet());
            assertEquals(currentRoomLocationZipCode, roomLocation.getZipCode());
            assertEquals(currentRoomLocationNumber, roomLocation.getNumber());
            assertEquals(currentRoomLocationLatitude, roomLocation.getLatitude());
            assertEquals(currentRoomLocationLongitude, roomLocation.getLongitude());
        });
        assertEquals(requestDto.getName(), resultDto.getName());
        assertEquals(requestDto.getDescription(), resultDto.getDescription());
    }

    @Test
    void itShouldUpdateRoomWithLocation() {
        //given
        String currentRoomLocationCity = "Poznan";
        String currentRoomLocationStreet = "AwesomeStreet";
        String currentRoomLocationZipCode = "60-688";
        int currentRoomLocationNumber = 10;
        double currentRoomLocationLatitude = 15;
        double currentRoomLocationLongitude = 15;
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(rooms.get(0).getId()).ifPresent(r -> {
            r.getFieldLocation().setCity(currentRoomLocationCity);
            r.getFieldLocation().setStreet(currentRoomLocationStreet);
            r.getFieldLocation().setZipCode(currentRoomLocationZipCode);
            r.getFieldLocation().setNumber(currentRoomLocationNumber);
            r.getFieldLocation().setLatitude(currentRoomLocationLatitude);
            r.getFieldLocation().setLongitude(currentRoomLocationLongitude);
            roomRepository.save(r);
        });
        long roomToUpdateId = rooms.get(0).getId();
        FieldLocationRequestDto locationRequestDto = FieldLocationRequestDto.builder()
                .city("Warsaw")
                .street("BoringStreet")
                .zipCode("70-700")
                .number(300)
                .build();
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .name("NewRoomName")
                .description("NewRoomDescription")
                .isPublic(true)
                .roomAdminId(user.getId())
                .location(locationRequestDto)
                .build();

        //when
        RoomEditResponseDto resultDto = roomService.updateRoom(roomToUpdateId, requestDto);

        //then
        assertNotNull(requestDto);
        assertNotNull(requestDto.getLocation());
        assertNotNull(resultDto.getLocation());
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(roomToUpdateId).ifPresent(r -> {
            FieldLocation roomLocation = r.getFieldLocation();

            assertEquals(requestDto.getName(), r.getName());
            assertEquals(requestDto.getDescription(), r.getDescription());
            assertEquals(requestDto.isPublic(), r.isPublic());
            assertEquals(locationRequestDto.getCity(), roomLocation.getCity());
            assertEquals(locationRequestDto.getStreet(), roomLocation.getStreet());
            assertEquals(locationRequestDto.getZipCode(), roomLocation.getZipCode());
            assertEquals(locationRequestDto.getNumber(), roomLocation.getNumber());
            assertTrue(roomLocation.getLatitude() != currentRoomLocationLatitude);
            assertTrue(roomLocation.getLongitude() != currentRoomLocationLongitude);
        });
        assertEquals(requestDto.getName(), resultDto.getName());
        assertEquals(requestDto.getDescription(), resultDto.getDescription());
        assertEquals(locationRequestDto.getCity(), resultDto.getLocation().getCity());
        assertEquals(locationRequestDto.getStreet(), resultDto.getLocation().getStreet());
        assertEquals(locationRequestDto.getZipCode(), resultDto.getLocation().getZipCode());
        assertEquals(locationRequestDto.getNumber(), resultDto.getLocation().getNumber());
    }

    @Test
    void itShouldUpdateNextMatchDatesSuccessfullyWithProvidedDatesInCorrectOrder() {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        LocalDateTime currentNextMatchDate = LocalDateTime.now().plusDays(5);
        LocalDateTime currentNextMatchRegistrationStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime currentNextMatchRegistrationEndDate = LocalDateTime.now().plusDays(4);

        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            r.setNextMatchDate(currentNextMatchDate);
            r.setNextMatchRegistrationStartDate(currentNextMatchRegistrationStartDate);
            r.setNextMatchRegistrationEndDate(currentNextMatchRegistrationEndDate);
            roomRepository.save(r);
        });
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in correct order
                .roomAdminId(user.getId())
                .nextMatchDate(now.plusDays(10).format(formatter))
                .nextMatchRegistrationStartDate(now.plusDays(2).format(formatter))
                .nextMatchRegistrationEndDate(now.plusDays(9).format(formatter))
                .build();

        //when
        roomService.updateNextMatchDates(roomToUpdateId, requestDto);

        //then
        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            LocalDateTime nextMatchDate = r.getNextMatchDate();
            LocalDateTime nextMatchRegistrationStartDate = r.getNextMatchRegistrationStartDate();
            LocalDateTime nextMatchRegistrationEndDate = r.getNextMatchRegistrationEndDate();

            assertTrue(nextMatchDate.isAfter(currentNextMatchDate));
            assertTrue(nextMatchRegistrationStartDate.isAfter(currentNextMatchRegistrationStartDate));
            assertTrue(nextMatchRegistrationEndDate.isAfter(currentNextMatchRegistrationEndDate));
            assertTrue(nextMatchDate.isAfter(nextMatchRegistrationStartDate) && nextMatchDate.isAfter(nextMatchRegistrationEndDate));
            assertTrue(nextMatchRegistrationEndDate.isAfter(nextMatchRegistrationStartDate));
        });
    }

    @Test
    void itShouldUpdateNextMatchDatesUnsuccessfullyWithProvidedDatesInIncorrectOrder() {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        LocalDateTime currentNextMatchDate = LocalDateTime.now().plusDays(5);
        LocalDateTime currentNextMatchRegistrationStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime currentNextMatchRegistrationEndDate = LocalDateTime.now().plusDays(4);

        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            r.setNextMatchDate(currentNextMatchDate);
            r.setNextMatchRegistrationStartDate(currentNextMatchRegistrationStartDate);
            r.setNextMatchRegistrationEndDate(currentNextMatchRegistrationEndDate);
            roomRepository.save(r);
        });
        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in incorrect order
                .roomAdminId(user.getId())
                .nextMatchDate(now.plusDays(10).format(formatter))
                .nextMatchRegistrationStartDate(now.plusDays(12).format(formatter))
                .nextMatchRegistrationEndDate(now.plusDays(11).format(formatter))
                .build();

        //when
        Exception exception = assertThrows(ResponseStatusException.class, () -> roomService.updateNextMatchDates(roomToUpdateId, requestDto));

        //then
        assertNotNull(exception);
        assertEquals(ResponseStatusException.class, exception.getClass());
        assertTrue(exception.getMessage().contains("Provided dates are in incorrect order or are not present."));
        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            LocalDateTime nextMatchDate = r.getNextMatchDate();
            LocalDateTime nextMatchRegistrationStartDate = r.getNextMatchRegistrationStartDate();
            LocalDateTime nextMatchRegistrationEndDate = r.getNextMatchRegistrationEndDate();

            assertEquals(currentNextMatchDate.getDayOfMonth(), nextMatchDate.getDayOfMonth());
            assertEquals(currentNextMatchRegistrationStartDate.getDayOfMonth(), nextMatchRegistrationStartDate.getDayOfMonth());
            assertEquals(currentNextMatchRegistrationEndDate.getDayOfMonth(), nextMatchRegistrationEndDate.getDayOfMonth());
        });
    }
}
