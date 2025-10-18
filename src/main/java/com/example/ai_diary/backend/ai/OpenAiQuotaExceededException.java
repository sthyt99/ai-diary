package com.example.ai_diary.backend.ai;

/**
 * 429(insufficient_quota)例外クラス
 */
public class OpenAiQuotaExceededException extends RuntimeException {

	public OpenAiQuotaExceededException(String msg) { super(msg); }
}
