package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.skills WHERE p.id = :id")
    Player findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT p FROM Player p INNER JOIN p.rooms rooms WHERE rooms.id = :id")
    List<Player> findAllByRoomId(@Param("id") Long id);
}
