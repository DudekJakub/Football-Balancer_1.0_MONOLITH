package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.SkillTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillTemplateRepository extends JpaRepository<SkillTemplate, Long> {

    List<SkillTemplate> findAllByActiveAndDefaultSkill(boolean active, boolean defaultSkill);
}
