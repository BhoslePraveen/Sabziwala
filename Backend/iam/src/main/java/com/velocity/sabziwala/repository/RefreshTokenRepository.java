package com.velocity.sabziwala.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.velocity.sabziwala.entity.RefreshToken;
import com.velocity.sabziwala.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
	
	Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);
	
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.user = :user AND rt.isRevoked = false")
    void revokeAllByUser(@Param("user") User user);
	

}
