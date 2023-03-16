package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.request.Request;
import com.dudek.footballbalancer.model.entity.request.RequestType;
import com.dudek.footballbalancer.model.entity.request.Requestable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Optional<Request> findByRequestableAndRequesterIdAndType(@NonNull Requestable requestable, @NonNull Long requesterId, @NonNull RequestType type);

}