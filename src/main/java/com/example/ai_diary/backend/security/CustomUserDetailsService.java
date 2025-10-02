package com.example.ai_diary.backend.security;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.exception.ErrorMessages;
import com.example.ai_diary.backend.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(ErrorMessages.USER_NOT_FOUND + email));

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(), user.getPasswordHash(), Collections.emptyList());
	}
}
