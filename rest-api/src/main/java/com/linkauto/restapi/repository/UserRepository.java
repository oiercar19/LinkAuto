package com.linkauto.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.linkauto.restapi.model.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_saved_posts WHERE user_username = :username", nativeQuery = true)
    void deleteSavedPostsByUsername(@Param("username") String username);

}

