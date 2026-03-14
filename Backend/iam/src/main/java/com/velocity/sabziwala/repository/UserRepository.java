package com.velocity.sabziwala.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.velocity.sabziwala.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByUserName(String userName);
	
	@Modifying
    @Query("UPDATE User u SET u.updatedAt = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
}
