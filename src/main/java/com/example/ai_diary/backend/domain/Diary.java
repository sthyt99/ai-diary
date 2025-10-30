package com.example.ai_diary.backend.domain;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 日記情報
 */
@Entity
@Table(name = "diaries")
public class Diary {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private Visibility visibility = Visibility.PRIVATE;

	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name="content_ai", columnDefinition="jsonb")
	private String contentAi;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	// --- getters/setters ---
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentAi() {
		return contentAi;
	}

	public void setContentAi(String contentAi) {
		this.contentAi = contentAi;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}