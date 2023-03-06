package com.dudek.footballbalancer.repository;

import com.dudek.footballbalancer.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(?1)")
    Optional<User> findByUsernameIgnoreCase(@NonNull String username);

    @Query("SELECT (COUNT(u) > 0) FROM User u WHERE UPPER(u.username) = UPPER(?1)")
    boolean existsByUsernameIgnoreCase(@NonNull String username);
}
