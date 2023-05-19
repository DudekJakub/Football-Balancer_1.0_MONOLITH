package com.dudek.footballbalancer.integration;

import com.dudek.footballbalancer.config.security.PasswordEncryptor;
import com.dudek.footballbalancer.model.Role;
import com.dudek.footballbalancer.model.SexStatus;
import com.dudek.footballbalancer.model.dto.fieldLocation.FieldLocationRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomEditRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomEnterRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewDatesRequestDto;
import com.dudek.footballbalancer.model.dto.room.RoomNewRequestDto;
import com.dudek.footballbalancer.model.entity.FieldLocation;
import com.dudek.footballbalancer.model.entity.Room;
import com.dudek.footballbalancer.model.entity.User;
import com.dudek.footballbalancer.repository.RoomRepository;
import com.dudek.footballbalancer.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMapAdapter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class RoomBasicManagementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncryptor passwordEncryptor;

    @Autowired
    private ObjectMapper objectMapper;

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
    void givenAnonymousUser_whenRequestPublicRooms_thenReturnListOfFiveDtos() throws Exception {
        //given
        MultiValueMapAdapter<String, String> params = new MultiValueMapAdapter<>(new HashMap<>());
        params.add("pageNo", "0");
        params.add("pageSize", "10");
        params.add("sortField", "id");
        params.add("sortDirection", "ASC");
        params.add("fetchPublic", "true");
        params.add("userId", "0");

        Room privateRoom1 = Room.builder()
                .name("TestPrivateRoomName1")
                .description("TestPrivateRoomDescription1")
                .password("TestRoomPassword")
                .isPublic(false)
                .build();
        Room privateRoom2 = Room.builder()
                .name("TestPrivateRoomName2")
                .description("TestPrivateRoomDescription2")
                .password("TestRoomPassword")
                .isPublic(false)
                .build();

        roomRepository.save(privateRoom1);
        roomRepository.save(privateRoom2);

        //when + then
        mockMvc.perform(get("/api/room/basic-management/paginated")
                        .accept(MediaType.APPLICATION_JSON)
                        .params(params))
                        .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(6))
                .andExpect(jsonPath("$[0].id").value(rooms.get(0).getId()))
                .andExpect(jsonPath("$[1].id").value(rooms.get(1).getId()))
                .andExpect(jsonPath("$[2].id").value(rooms.get(2).getId()))
                .andExpect(jsonPath("$[0].name").value(rooms.get(0).getName()))
                .andExpect(jsonPath("$[1].name").value(rooms.get(1).getName()))
                .andExpect(jsonPath("$[2].name").value(rooms.get(2).getName()));

        assertEquals(8, roomRepository.count());
        assertEquals(6, roomRepository.findAll()
                .stream()
                .filter(Room::isPublic)
                .count());
    }

    @Test
    void givenAuthorizedUser_whenRequestPublicRooms_thenReturnListOfDtosExceptUserOwned() throws Exception {
        //given
        MultiValueMapAdapter<String, String> params = new MultiValueMapAdapter<>(new HashMap<>());
        params.add("pageNo", "0");
        params.add("pageSize", "10");
        params.add("sortField", "id");
        params.add("sortDirection", "ASC");
        params.add("fetchPublic", "true");
        params.add("userId", user.getId().toString());

        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        //when + then
        mockMvc.perform(get("/api/room/basic-management/paginated")
                        .accept(MediaType.APPLICATION_JSON)
                        .params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(5));

        assertEquals(6, roomRepository.count());
    }

    @Test
    void givenAuthorizedUser_whenRequestByUserId_thenReturnListOfOwnedDtos() throws Exception {
        //given
        MultiValueMapAdapter<String, String> params = new MultiValueMapAdapter<>(new HashMap<>());
        params.add("pageNo", "0");
        params.add("pageSize", "10");
        params.add("sortField", "id");
        params.add("sortDirection", "ASC");

        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        //when + then
        mockMvc.perform(get("/api/room/basic-management/paginated/" + user.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1));

        assertEquals(6, roomRepository.count());
    }

    @Test
    void givenAuthorizedUser_whenRequestSearchByRoomName_thenReturnListOfSpecificDto() throws Exception {
        //when + then
        mockMvc.perform(get("/api/room/basic-management/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("searchItem", "TestRoomName1")
                        .param("userId", user.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].userInRoom").value(false));

        assertEquals(6, roomRepository.count());
    }

    @Test
    void givenAuthorizedUser_whenRequestSearchByOwnedRoomName_thenReturnListOfSpecificDto() throws Exception {
        //given
        roomRepository.findById(rooms.get(0).getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        //when + then
        mockMvc.perform(get("/api/room/basic-management/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("searchItem", "TestRoomName1")
                        .param("userId", user.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].userInRoom").value(true));

        assertEquals(6, roomRepository.count());
    }

    @Test
    void givenAuthorizedUser_whenRequestRoomEnterWithCorrectPassword_thenReturnOk() throws Exception {
        //given
        Room targetRoom = rooms.get(0);
        roomRepository.findById(targetRoom.getId()).ifPresent(r -> {
            passwordEncryptor.encrypt(r.getPassword()).ifPresent(r::setPassword);
            roomRepository.save(r);
        });

        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .userId(user.getId())
                .roomId(targetRoom.getId())
                .password("TestRoomPassword1")
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(targetRoom.getName()))
                .andExpect(jsonPath("$.description").value(targetRoom.getDescription()))
                .andExpect(jsonPath("$.users.size()").value(0));
    }

    @Test
    void givenAuthorizedUser_whenRequestRoomEnterWithIncorrectPassword_thenReturnUnauthorized() throws Exception {
        //given
        Room targetRoom = rooms.get(0);
        roomRepository.findById(targetRoom.getId()).ifPresent(r -> {
            passwordEncryptor.encrypt(r.getPassword()).ifPresent(r::setPassword);
            roomRepository.save(r);
        });

        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .userId(user.getId())
                .roomId(targetRoom.getId())
                .password("TestRoomPassword10")
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenAuthorizedUser_whenRequestRoomEnterWhichIsMemberOf_thenReturnOk() throws Exception {
        //given
        Room targetRoom = rooms.get(0);
        roomRepository.findById(targetRoom.getId()).ifPresent(r -> {
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        RoomEnterRequestDto requestDto = RoomEnterRequestDto.builder()
                .userId(user.getId())
                .roomId(targetRoom.getId())
                .password("") //password is not required if user is already room's member
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management/enter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(targetRoom.getName()))
                .andExpect(jsonPath("$.description").value(targetRoom.getDescription()))
                .andExpect(jsonPath("$.users.size()").value(1));
    }

    @Test
    void givenAuthorizedUser_whenRequestNewPublicRoomWithoutLocation_thenCreateOneAndReturnOk() throws Exception {
        //given
        long currentRoomsQuantity = roomRepository.count();
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestRoomName7")
                .roomPassword("TestRoomPassword7")
                .location(null)
                .isPublic(true)
                .userRequestSenderId(user.getId())
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rooms.get(5).getId() + 1))
                .andExpect(jsonPath("$.name").value(requestDto.getRoomName()))
                .andExpect(jsonPath("$.public").value(requestDto.isPublic()))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.location.city").value("N/A"))
                .andExpect(jsonPath("$.location.street").value("N/A"))
                .andExpect(jsonPath("$.location.zipCode").value("00-000"))
                .andExpect(jsonPath("$.location.number").value(0))
                .andExpect(jsonPath("$.admins.size()").value(1))
                .andExpect(jsonPath("$.users.size()").value(1))
                .andExpect(jsonPath("$.admins[0].id").value(user.getId()))
                .andExpect(jsonPath("$.users[0].id").value(user.getId()));

        Room newRoom = roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(requestDto.getRoomName(), "", "").get(0);

        assertEquals(currentRoomsQuantity + 1, roomRepository.count());
        assertEquals(requestDto.getRoomName(), newRoom.getName());
        assertTrue(passwordEncryptor.checkPasswordMatch(newRoom.getPassword(), requestDto.getRoomPassword()));
    }

    @Test
    void givenAuthorizedUser_whenRequestNewPrivateRoomWithoutLocation_thenCreateOneAndReturnOk() throws Exception {
        //given
        long currentRoomsQuantity = roomRepository.count();
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestRoomName7")
                .roomPassword("TestRoomPassword7")
                .location(null)
                .isPublic(false)
                .userRequestSenderId(user.getId())
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rooms.get(5).getId() + 1))
                .andExpect(jsonPath("$.name").value(requestDto.getRoomName()))
                .andExpect(jsonPath("$.public").value(requestDto.isPublic()))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.location.city").value("N/A"))
                .andExpect(jsonPath("$.location.street").value("N/A"))
                .andExpect(jsonPath("$.location.zipCode").value("00-000"))
                .andExpect(jsonPath("$.location.number").value(0))
                .andExpect(jsonPath("$.admins.size()").value(1))
                .andExpect(jsonPath("$.users.size()").value(1))
                .andExpect(jsonPath("$.admins[0].id").value(user.getId()))
                .andExpect(jsonPath("$.users[0].id").value(user.getId()));

        Room newRoom = roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(requestDto.getRoomName(), "", "").get(0);

        assertEquals(currentRoomsQuantity + 1, roomRepository.count());
        assertEquals(requestDto.getRoomName(), newRoom.getName());
        assertFalse(newRoom.isPublic());
        assertTrue(passwordEncryptor.checkPasswordMatch(newRoom.getPassword(), requestDto.getRoomPassword()));
    }

    @Test
    void givenAuthorizedUser_whenRequestNewPublicRoomWithLocation_thenCreateOneAndReturnOk() throws Exception {
        //given
        long currentRoomsQuantity = roomRepository.count();
        FieldLocationRequestDto locationRequestDto = FieldLocationRequestDto.builder()
                .city("Warsaw")
                .street("AwesomeStreet")
                .zipCode("70-000")
                .number(10)
                .build();
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestRoomName7")
                .roomPassword("TestRoomPassword7")
                .location(locationRequestDto)
                .isPublic(false)
                .userRequestSenderId(user.getId())
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rooms.get(5).getId() + 1))
                .andExpect(jsonPath("$.name").value(requestDto.getRoomName()))
                .andExpect(jsonPath("$.public").value(requestDto.isPublic()))
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.location.city").value(locationRequestDto.getCity()))
                .andExpect(jsonPath("$.location.street").value(locationRequestDto.getStreet()))
                .andExpect(jsonPath("$.location.zipCode").value(locationRequestDto.getZipCode()))
                .andExpect(jsonPath("$.location.number").value(locationRequestDto.getNumber()))
                .andExpect(jsonPath("$.location.latitude").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.location.longitude").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.admins.size()").value(1))
                .andExpect(jsonPath("$.users.size()").value(1))
                .andExpect(jsonPath("$.admins[0].id").value(user.getId()))
                .andExpect(jsonPath("$.users[0].id").value(user.getId()));

        Room newRoom = roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(requestDto.getRoomName(), "", "").get(0);

        assertEquals(currentRoomsQuantity + 1, roomRepository.count());
        assertEquals(requestDto.getRoomName(), newRoom.getName());
        assertTrue(passwordEncryptor.checkPasswordMatch(newRoom.getPassword(), requestDto.getRoomPassword()));
    }

    @Test
    void givenAnonymousUser_whenRequestNewRoom_thenReturnNotFound() throws Exception {
        //given
        long currentRoomsQuantity = roomRepository.count();
        RoomNewRequestDto requestDto = RoomNewRequestDto.builder()
                .roomName("TestRoomName7")
                .roomPassword("TestRoomPassword7")
                .location(null)
                .isPublic(false)
                .userRequestSenderId(0L) //not provided
                .build();

        //when + then
        mockMvc.perform(post("/api/room/basic-management")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        List<Room> resultRooms = roomRepository.findByNameOrFieldLocationCityOrFieldLocationStreet(requestDto.getRoomName(), "", "");

        assertEquals(currentRoomsQuantity, roomRepository.count());
        assertEquals(0, resultRooms.size());
    }

    @Test
    void givenAuthorizedRoomAdmin_whenRequestUpdateRoom_thenUpdateOneAndReturnOk() throws Exception {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            FieldLocation location = FieldLocation.builder()
                    .city("N/A")
                    .street("N/A")
                    .zipCode("00-000")
                    .number(0)
                    .build();

            r.setUsersInRoom(Set.of(user));
            r.setAdminsInRoom(Set.of(user));
            r.setFieldLocation(location);
            roomRepository.save(r);
        });

        long currentRoomsQuantity = roomRepository.count();
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .roomAdminId(user.getId())
                .name("UpdatedRoomName")
                .description("UpdatedRoomDescription")
                .isPublic(true)
                .location(null)
                .build();

        //when + then
        mockMvc.perform(put("/api/room/basic-management/" + roomToUpdateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(requestDto.getName()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()))
                .andExpect(jsonPath("$.public").value(requestDto.isPublic()))
                .andExpect(jsonPath("$.location").value(nullValue()));

        assertEquals(currentRoomsQuantity, roomRepository.count());
        roomRepository.findByIdFetchLocationAndPlayersUsersAdmins(roomToUpdateId).ifPresent(r -> {
            assertEquals(requestDto.getName(), r.getName());
            assertEquals(requestDto.getDescription(), r.getDescription());
            assertEquals(requestDto.isPublic(), r.isPublic());
            assertEquals("N/A", r.getFieldLocation().getCity());
            assertEquals("N/A", r.getFieldLocation().getStreet());
            assertEquals("00-000", r.getFieldLocation().getZipCode());
            assertEquals(0, r.getFieldLocation().getNumber());
        });
    }

    @Test
    void givenAuthorizedRoomAdmin_whenRequestUpdateRoomThatDoesntExist_thenReturnNotFound() throws Exception {
        //given
        long roomToUpdateId = 0L; //room doesn't exist
        long currentRoomsQuantity = roomRepository.count();
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .roomAdminId(user.getId())
                .name("UpdatedRoomName")
                .description("UpdatedRoomDescription")
                .isPublic(true)
                .location(null)
                .build();

        //when + then
        mockMvc.perform(put("/api/room/basic-management/" + roomToUpdateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        assertEquals(currentRoomsQuantity, roomRepository.count());
    }

    @Test
    void givenAuthorizedUserWhichIsNotRoomAdmin_whenRequestUpdateRoom_thenReturnUnauthorized() throws Exception {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        long currentRoomsQuantity = roomRepository.count();
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .roomAdminId(user.getId()) //not room's admin
                .name("UpdatedRoomName")
                .description("UpdatedRoomDescription")
                .isPublic(true)
                .location(null)
                .build();

        //when + then
        mockMvc.perform(put("/api/room/basic-management/" + roomToUpdateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        assertEquals(currentRoomsQuantity, roomRepository.count());
    }

    @Test
    void givenAnonymousUser_whenRequestUpdateRoom_thenReturnNotFound() throws Exception {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        long currentRoomsQuantity = roomRepository.count();
        RoomEditRequestDto requestDto = RoomEditRequestDto.builder()
                .roomAdminId(0L) //not provided
                .name("UpdatedRoomName")
                .description("UpdatedRoomDescription")
                .isPublic(true)
                .location(null)
                .build();

        //when + then
        mockMvc.perform(put("/api/room/basic-management/" + roomToUpdateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());

        assertEquals(currentRoomsQuantity, roomRepository.count());
    }

    @Test
    void givenAuthorizedRoomAdmin_whenRequestUpdateNextRoomMatchDatesWithCorrectDates_thenPatchGivenRoomAndReturnOk() throws Exception {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        long currentRoomsQuantity = roomRepository.count();

        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            r.setAdminsInRoom(Set.of(user));
            r.setUsersInRoom(Set.of(user));
            roomRepository.save(r);
        });

        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime updatedNextMatchDate = now.plusDays(5L);
        OffsetDateTime updatedNextRegistrationStartDate = now.plusDays(1L);
        OffsetDateTime updatedNextRegistrationEndDate = now.plusDays(4L);

        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in correct order
                .roomAdminId(user.getId())
                .nextMatchDate(updatedNextMatchDate.format(formatter))
                .nextMatchRegistrationStartDate(updatedNextRegistrationStartDate.format(formatter))
                .nextMatchRegistrationEndDate(updatedNextRegistrationEndDate.format(formatter))
                .build();

        //when + then
        mockMvc.perform(patch("/api/room/basic-management/next-match-all-dates/" + roomToUpdateId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent())
                .andExpect(content().string("")); //void method

        assertEquals(currentRoomsQuantity, roomRepository.count());
        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            assertEquals(updatedNextMatchDate.getDayOfMonth(), r.getNextMatchDate().getDayOfMonth());
            assertEquals(updatedNextRegistrationStartDate.getDayOfMonth(), r.getNextMatchRegistrationStartDate().getDayOfMonth());
            assertEquals(updatedNextRegistrationEndDate.getDayOfMonth(), r.getNextMatchRegistrationEndDate().getDayOfMonth());
        });
    }

    @Test
    void givenAuthorizedRoomAdmin_whenRequestUpdateNextRoomMatchDatesWithIncorrectDates_thenReturnBadRequest() throws Exception {
        //given
        long roomToUpdateId = rooms.get(0).getId();
        long currentRoomsQuantity = roomRepository.count();

        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            r.setAdminsInRoom(Set.of(user));
            r.setUsersInRoom(Set.of(user));
            r.setNextMatchDate(LocalDateTime.now().plusDays(3));
            r.setNextMatchRegistrationStartDate(LocalDateTime.now().plusDays(1));
            r.setNextMatchRegistrationEndDate(LocalDateTime.now().plusDays(2));
            roomRepository.save(r);
        });

        OffsetDateTime now = OffsetDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        OffsetDateTime updatedNextMatchDate = now.plusDays(5L);
        OffsetDateTime updatedNextRegistrationStartDate = now.plusDays(7L);
        OffsetDateTime updatedNextRegistrationEndDate = now.plusDays(6L);

        RoomNewDatesRequestDto requestDto = RoomNewDatesRequestDto.builder() //provided dates are in incorrect order
                .roomAdminId(user.getId())
                .nextMatchDate(updatedNextMatchDate.format(formatter))
                .nextMatchRegistrationStartDate(updatedNextRegistrationStartDate.format(formatter))
                .nextMatchRegistrationEndDate(updatedNextRegistrationEndDate.format(formatter))
                .build();

        //when + then
        mockMvc.perform(patch("/api/room/basic-management/next-match-all-dates/" + roomToUpdateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        assertEquals(currentRoomsQuantity, roomRepository.count());
        roomRepository.findById(roomToUpdateId).ifPresent(r -> {
            assertEquals(LocalDateTime.now().plusDays(3).getDayOfMonth(), r.getNextMatchDate().getDayOfMonth());
            assertEquals(LocalDateTime.now().plusDays(1).getDayOfMonth(), r.getNextMatchRegistrationStartDate().getDayOfMonth());
            assertEquals(LocalDateTime.now().plusDays(2).getDayOfMonth(), r.getNextMatchRegistrationEndDate().getDayOfMonth());
        });
    }
}