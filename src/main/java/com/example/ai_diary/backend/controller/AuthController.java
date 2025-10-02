package com.example.ai_diary.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.dto.AuthDtos.AuthResponse;
import com.example.ai_diary.backend.dto.AuthDtos.LoginRequest;
import com.example.ai_diary.backend.dto.AuthDtos.SignupRequest;
import com.example.ai_diary.backend.service.UserService;

import jakarta.validation.Valid;

/**
 * 認証用コントローラ
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserService userService;
	private final AuthenticationManager authManager;
	private final JwtUtil jwtUtil;

	public AuthController(UserService userService, AuthenticationManager authManager, JwtUtil jwtUtil) {
		this.userService = userService;
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
		User user = userService.signup(req.getEmail(), req.getPassword(), req.getDisplayName());
		String token = jwtUtil.generateToken(user.getEmail());
		return ResponseEntity.ok(
				new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName(), user.isPremiumFlag()));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
		Authentication auth = authManager
				.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

		UserDetails ud = (UserDetails) auth.getPrincipal();
		User user = userService.findByEmailOrThrow(ud.getUsername());
		String token = jwtUtil.generateToken(user.getEmail());
		return ResponseEntity.ok(
				new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName(), user.isPremiumFlag()));
	}

}
