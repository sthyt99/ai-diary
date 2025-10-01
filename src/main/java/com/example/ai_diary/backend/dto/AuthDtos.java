package com.example.ai_diary.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 認証DTO関連
 */
public class AuthDtos {
	
	/** サインアップ（受取） */
	public static class SignupRequest {
		
		@Email
		@NotBlank
		private String email;
		
		@NotBlank
		@Size(min = 8, max = 128)
		private String password;
		
		@NotBlank @Size(max = 64)
		private String displayName;
		
		// getters/setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
	}
	
	/** ログイン（受取） */
	public static class LoginRequest {
		
		@Email
		@NotBlank
		private String email;
		
		@NotBlank
		private String password;
		
		// getters/setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
	}
	
	/** 認証（送信） */
	public static class AuthResponse {
		
		private String token;
		private Long userId;
		private String email;
		private String displayName;
		private boolean premium;
		
		public AuthResponse() {}
		
		public AuthResponse(String token, Long userId, String email, String displayName, boolean premium) {
            this.token = token;
            this.userId = userId;
            this.email = email;
            this.displayName = displayName;
            this.premium = premium;
        }
		
		// getters/setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public boolean isPremium() { return premium; }
        public void setPremium(boolean premium) { this.premium = premium; }
	}
}
