package com.example.ai_diary.backend.service;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.exception.ErrorMessages;
import com.example.ai_diary.backend.repository.UserRepository;
import com.example.ai_diary.backend.util.EmailNormalizer;

/**
 * ユーザーサービス
 */
@Service
public class UserService {

	/**
	 * メールアドレス構成
	 */
	private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;

	public UserService(UserRepository userRepository, PasswordEncoder encoder) {
		this.userRepository = userRepository;
		this.encoder = encoder;
	}

	/**
	 * サインアップ処理
	 */
	public User signup(String email, String rawPassword, String displayName) {

		// ---- 入力バリデーション ----
		String norm = EmailNormalizer.normalize(email);
		if (norm == null || norm.isEmpty() || !SIMPLE_EMAIL.matcher(norm).matches()) {
			throw new IllegalArgumentException(ErrorMessages.INVALID_EMAIL);
		}
		if (rawPassword == null || rawPassword.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorMessages.PASSWORD_REQUIRED);
		}
		// 強度チェックは必要に応じて（最低8文字など）
		if (rawPassword.length() < 8) {
			throw new IllegalArgumentException(ErrorMessages.PASSWORD_TOO_WEAK); // 例: 「パスワードは8文字以上」
		}
		if (displayName == null || displayName.trim().isEmpty()) {
			throw new IllegalArgumentException(ErrorMessages.DISPLAY_NAME_REQUIRED);
		}

		// ---- ビジネスルール ----
		if (userRepository.existsByEmail(norm)) {
			throw new IllegalArgumentException(ErrorMessages.EMAIL_ALREADY_USED);
		}
		User u = new User();
		u.setEmail(norm);
		u.setPasswordHash(encoder.encode(rawPassword));
		u.setDisplayName(displayName.trim());
		return userRepository.save(u);
	}

	/**
	 * メールアドレス取得処理
	 */
	public User findByEmailOrThrow(String email) {
		String norm = EmailNormalizer.normalize(email);
		if (norm == null || norm.isEmpty() || !SIMPLE_EMAIL.matcher(norm).matches()) {
			throw new IllegalArgumentException(ErrorMessages.INVALID_EMAIL);
		}
		return userRepository.findByEmail(norm).orElseThrow();
	}
}
