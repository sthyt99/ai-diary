package com.example.ai_diary.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * JwtUtil のユニットテスト
 */
class JwtUtilTest {

	// Keys.hmacShaKeyFor(...) は 32byte (256bit) 以上が必要
	private static final String SECRET_64B = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!!"; // 66文字
	private static final String OTHER_SECRET_64B = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ!!";

	@Test
	@DisplayName("generateToken → validate=true & extractSubject が一致")
	void generate_validate_extract_ok() {
		JwtUtil jwt = new JwtUtil(SECRET_64B, 60_000L); // 60秒

		String subject = "alice@example.com";
		String token = jwt.generateToken(subject);

		assertNotNull(token);
		assertTrue(jwt.validate(token));
		assertEquals(subject, jwt.extractSubject(token));
	}

	@Test
	@DisplayName("期限切れトークンは validate=false になり、extract は ExpiredJwtException")
	void expired_token_is_invalid() throws InterruptedException {
		JwtUtil shortJwt = new JwtUtil(SECRET_64B, 1L); // 1ms で期限切れにする
		String token = shortJwt.generateToken("bob@example.com");

		// 期限切れになるまで少し待つ
		Thread.sleep(3);

		assertFalse(shortJwt.validate(token));
		assertThrows(ExpiredJwtException.class, () -> shortJwt.extractSubject(token));
	}

	@Test
	@DisplayName("改ざんトークンは validate=false かつ extract は JwtException")
	void tampered_token_is_invalid() {
		JwtUtil jwt = new JwtUtil(SECRET_64B, 60_000L);
		String token = jwt.generateToken("carol@example.com");

		// 末尾を改変して署名不一致にする
		String tampered = token.substring(0, token.length() - 2) + "aa";

		assertFalse(jwt.validate(tampered));
		assertThrows(JwtException.class, () -> jwt.extractSubject(tampered));
	}

	@Test
	@DisplayName("別キーで検証すると validate=false かつ extract は JwtException")
	void token_signed_with_other_key_fails_validation() {
		JwtUtil jwt = new JwtUtil(SECRET_64B, 60_000L);
		String token = jwt.generateToken("dave@example.com");

		JwtUtil otherKeyJwt = new JwtUtil(OTHER_SECRET_64B, 60_000L);

		assertFalse(otherKeyJwt.validate(token));
		assertThrows(JwtException.class, () -> otherKeyJwt.extractSubject(token));
	}

	@Test
	@DisplayName("不正フォーマットのトークンは validate=false かつ extract は JwtException")
	void malformed_token_is_invalid() {
		JwtUtil jwt = new JwtUtil(SECRET_64B, 60_000L);

		String malformed = "not-a-jwt";
		assertFalse(jwt.validate(malformed));
		assertThrows(JwtException.class, () -> jwt.extractSubject(malformed));
	}
}
