package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {
}