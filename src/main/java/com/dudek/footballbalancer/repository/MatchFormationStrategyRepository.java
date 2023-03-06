package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.MatchFormationStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchFormationStrategyRepository extends JpaRepository<MatchFormationStrategy, Long> {
}
