package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.room WHERE p.room.id = :roomId")
    List<Player> findAllByRoomIdFetchRoom(@Param("roomId") Long roomId);

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.skills WHERE p.id = :id")
    Optional<Player> findByIdFetchSkills(@Param("id") Long id);

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.user WHERE p.id = :id")
    Optional<Player> findByIdFetchUser(@Param("id") Long id);

    @Query("SELECT COUNT(p) > 0 FROM Player p WHERE p.user.id = :userId")
    boolean isAnyPlayerLinkedWithUserByUserId(@Param("userId") Long userId);
}
