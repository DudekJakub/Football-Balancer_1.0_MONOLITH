package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.adminsInRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchAdmins(@NonNull Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.playersInRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchPlayersInRoom(@NonNull Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.skillTemplatesForRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchSkillTemplates(@NonNull Long id);

    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.playersInRoom " +
            "LEFT JOIN FETCH r.usersInRoom " +
            "LEFT JOIN FETCH r.adminsInRoom " +
            "WHERE r.id = :id")
    Optional<Room> findByIdFetchPlayersAndUsers(@Param("id") Long id);

    @Query(value = "SELECT r FROM Room r " +
                    "LEFT JOIN FETCH r.usersInRoom u " +
                    "LEFT JOIN FETCH r.fieldLocation " +
                    "WHERE r.isPublic = :fetchPublic AND u.id <> :userId",
          countQuery = "SELECT COUNT(r) FROM Room r LEFT JOIN r.usersInRoom LEFT JOIN r.fieldLocation")
    Page<Room> findPaginatedFetchUsersInRoomAndLocation(Pageable pageable,@Param("fetchPublic") boolean fetchPublic, Long userId);

    @Query(value = "SELECT r FROM Room r " +
                    "LEFT JOIN FETCH r.usersInRoom user " +
                    "LEFT JOIN FETCH r.fieldLocation " +
                    "WHERE user.id = :userId",
           countQuery = "SELECT COUNT(r) FROM Room r LEFT JOIN r.usersInRoom user LEFT JOIN r.fieldLocation")
    Page<Room> findPaginatedFetchUsersInRoomAndLocationByUserId(Pageable pageable, @Param("userId") Long userId);

    List<Room> findByNameContainsIgnoreCaseOrFieldLocation_CityContainsIgnoreCaseOrFieldLocation_StreetContainsIgnoreCase(@NotBlank @Size(min = 3, max = 30) String name, String fieldLocation_city, String fieldLocation_street);
}
