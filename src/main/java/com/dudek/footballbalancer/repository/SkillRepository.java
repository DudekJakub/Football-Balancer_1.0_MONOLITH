package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    @Query("SELECT s FROM Skill s LEFT JOIN FETCH s.skillTemplate WHERE s.player.id = :playerId")
    List<Skill> findAllByPlayerIdFetchSkillTemplates(@Param("playerId") Long playerId);
}
