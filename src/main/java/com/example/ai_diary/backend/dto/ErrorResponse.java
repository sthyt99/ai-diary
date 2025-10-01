package com.example.ai_diary.backend.dto;

import java.time.Instant;
import java.util.List;

/**
 * エラー（返却）
 */
public class ErrorResponse {

	private Instant timestamp;
	private int status;
	private String error;
	
	// 独自のエラーコード
	private String code;
	// 短い説明
	private String message;
	// リクエストパス
	private String path;
	// 具体的なエラー詳細
	private List<String> details;

	public ErrorResponse() {
	}

	public ErrorResponse(Instant timestamp, int status, String error, String code,
			String message, String path, List<String> details) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.code = code;
		this.message = message;
		this.path = path;
		this.details = details;
	}
	
	// --- getters/setters ---
	public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
