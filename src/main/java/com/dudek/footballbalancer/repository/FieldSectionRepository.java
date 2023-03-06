package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.FieldSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldSectionRepository extends JpaRepository<FieldSection, Long> {
}
