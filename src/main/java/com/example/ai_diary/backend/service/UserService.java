package com.example.ai_diary.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.exception.ErrorMessages;
import com.example.ai_diary.backend.repository.UserRepository;

/**
 * ユーザーサービス
 */
@Service
public class UserService {

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
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException(ErrorMessages.EMAIL_ALREADY_USED);
		}
		User u = new User();
		u.setEmail(email);
		u.setPasswordHash(encoder.encode(rawPassword));
		u.setDisplayName(displayName);
		return userRepository.save(u);
	}

	/**
	 * メールアドレス取得処理
	 */
	public User findByEmailOrThrow(String email) {
		return userRepository.findByEmail(email).orElseThrow();
	}
}
