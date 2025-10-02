package com.example.ai_diary.backend.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final Key key;
	private final long expirationMs; // 有効期限ミリ秒

	public JwtUtil(@Value("$(security.jwt.secret)") String secret,
			@Value("$(security.jwt.expiration-ms)") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMs = expirationMs;
	}

	public String generateToken(String subject) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + expirationMs))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public String extractSubject(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build()
				.parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validate(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
