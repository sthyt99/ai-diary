package com.example.ai_diary.backend.dto;

import java.time.Instant;

import com.example.ai_diary.backend.domain.Visibility;

import jakarta.validation.constraints.NotBlank;

public class DiaryDtos {

	/**
	 * 日記作成（受取）
	 */
	public static class CreateRequest {

		@NotBlank
		private String content;
		private Visibility visibility;
		
		// getters/setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Visibility getVisibility() { return visibility; }
        public void setVisibility(Visibility visibility) { this.visibility = visibility; }
	}
	
	/**
	 * 日記情報（送信）
	 */
	public static class Response {
		
		private Long id;
		private Long userId;
		private String content;
		private String contentAi;
		private Visibility visibility;
		private Instant createdAt;
		
		// getters/setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getContentAi() { return contentAi; }
        public void setContentAi(String contentAi) { this.contentAi = contentAi; }
        public Visibility getVisibility() { return visibility; }
        public void setVisibility(Visibility visibility) { this.visibility = visibility; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	}
}
