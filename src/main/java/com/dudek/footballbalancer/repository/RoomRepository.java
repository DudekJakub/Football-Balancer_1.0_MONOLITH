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

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.adminsInRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchAdmins(@NonNull Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.playersInRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchPlayersInRoom(@NonNull Long id);

    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.skillTemplatesForRoom WHERE r.id = ?1")
    Optional<Room> findByIdFetchSkillTemplates(@NonNull Long id);

    @Query("SELECT r FROM Room r " +
            "LEFT JOIN FETCH r.fieldLocation " +
            "LEFT JOIN FETCH r.playersInRoom " +
            "LEFT JOIN FETCH r.usersInRoom " +
            "LEFT JOIN FETCH r.adminsInRoom " +
            "WHERE r.id = :id")
    Optional<Room> findByIdFetchLocationAndPlayersUsersAdmins(@Param("id") Long id);

    @Query(value = "SELECT r FROM Room r " +
                    "LEFT JOIN FETCH r.usersInRoom " +
                    "LEFT JOIN FETCH r.fieldLocation " +
                    "WHERE r.isPublic = :fetchPublic AND NOT EXISTS (SELECT 1 FROM r.usersInRoom ur WHERE ur.id = :userId)",
          countQuery = "SELECT COUNT(r) FROM Room r LEFT JOIN r.usersInRoom LEFT JOIN r.fieldLocation")
    Page<Room> findPaginatedFetchUsersInRoomAndLocation(Pageable pageable, @Param("fetchPublic") boolean fetchPublic, @Param("userId") Long userId);

    @Query(value = "SELECT r FROM Room r " +
                    "LEFT JOIN FETCH r.usersInRoom user " +
                    "LEFT JOIN FETCH r.fieldLocation " +
                    "WHERE r IN (SELECT DISTINCT r1 FROM Room r1 LEFT JOIN r1.usersInRoom u1 WHERE u1.id = :userId)",
           countQuery = "SELECT COUNT(r) FROM Room r LEFT JOIN r.usersInRoom user LEFT JOIN r.fieldLocation")
    Page<Room> findPaginatedFetchUsersInRoomAndLocationByUserId(Pageable pageable, @Param("userId") Long userId);

    @Query(value = "SELECT r FROM Room r " +
                     "LEFT JOIN FETCH r.usersInRoom u " +
                     "LEFT JOIN FETCH r.fieldLocation fL " +
                     "WHERE LOWER(r.name) = LOWER(:name) " +
                     "OR LOWER(fL.city) = LOWER(:fieldLocation_city) " +
                     "OR LOWER(fL.street) = LOWER(:fieldLocation_street)")
    List<Room> findByNameOrFieldLocationCityOrFieldLocationStreet(@NotBlank @Size(min = 3, max = 30) String name, String fieldLocation_city, String fieldLocation_street);

    @Query(value = "SELECT COUNT(r) > 0 FROM Room r " +
                    "JOIN r.usersInRoom u WHERE r.id = :roomId AND u.id = :userId")
    boolean isUserMemberOfRoom(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query(value = "SELECT COUNT(r) > 0 FROM Room r " +
                    "JOIN r.adminsInRoom u WHERE r.id = :roomId AND u.id = :adminId")
    boolean isAdminOfRoom(@Param("adminId") Long adminId, @Param("roomId") Long roomId);
}
