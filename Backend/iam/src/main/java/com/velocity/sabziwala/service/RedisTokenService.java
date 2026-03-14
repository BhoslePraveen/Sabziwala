package com.velocity.sabziwala.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis-backed token management service.
 *
 * Redis Key Strategy:
 * ┌───────────────────────────────────────────────────────┐
 * │  blacklist:access:{token}   → "1"   (TTL = token TTL) │
 * │  refresh:{userId}           → token  (TTL = 7 days)   │
 * │  session:{userId}           → "1"    (TTL = 7 days)   │
 * └───────────────────────────────────────────────────────┘
 *
 * Why Redis for tokens?
 * - O(1) lookup for blacklist checks on every request
 * - Auto-expiry via TTL (no manual cleanup needed)
 * - Shared across multiple instances of the IAM service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenService {

	private final StringRedisTemplate redisTemplate;

	private static final String BLACKLIST_PREFIX = "blacklist:access:";
	private static final String REFRESH_PREFIX = "refresh:";
	private static final String SESSION_PREFIX = "session:";

	/**
	 * Blacklist an access token when user logs out. The token is stored with its
	 * remaining TTL so it auto-expires.
	 */
	public void blacklistAccessToken(String token, long ttlMs) {
		String key = BLACKLIST_PREFIX + token;
		redisTemplate.opsForValue().set(key, "1", ttlMs, TimeUnit.MILLISECONDS);
		log.debug("Blacklisted access token, TTL: {}ms", ttlMs);
	}

	/**
	 * Check if an access token has been blacklisted.
	 */
	public boolean isAccessTokenBlacklisted(String token) {
		String key = BLACKLIST_PREFIX + token;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	/**
	 * Cache refresh token in Redis for fast lookup.
	 */
	public void cacheRefreshToken(String userId, String refreshToken, long ttlMs) {
		String key = REFRESH_PREFIX + userId;
		redisTemplate.opsForValue().set(key, refreshToken, ttlMs, TimeUnit.MILLISECONDS);
		log.debug("Cached refresh token for user: {}", userId);
	}

	/**
	 * Get cached refresh token for a user.
	 */
	public String getCachedRefreshToken(String userId) {
		String key = REFRESH_PREFIX + userId;
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * Remove cached refresh token (on logout or rotation).
	 */
	public void removeCachedRefreshToken(String userId) {
		String key = REFRESH_PREFIX + userId;
		redisTemplate.delete(key);
		log.debug("Removed cached refresh token for user: {}", userId);
	}

	/**
	 * Mark user session as active.
	 */
	public void createSession(String userId, long ttlMs) {
		String key = SESSION_PREFIX + userId;
		redisTemplate.opsForValue().set(key, "1", ttlMs, TimeUnit.MILLISECONDS);
	}

	/**
	 * Check if user has an active session.
	 */
	public boolean hasActiveSession(String userId) {
		String key = SESSION_PREFIX + userId;
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	/**
	 * Destroy user session.
	 */
	public void destroySession(String userId) {
		String key = SESSION_PREFIX + userId;
		redisTemplate.delete(key);
	}

}
