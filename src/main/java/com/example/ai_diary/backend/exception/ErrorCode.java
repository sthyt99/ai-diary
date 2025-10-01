package com.example.ai_diary.backend.exception;

/**
 * エラーコード
 */
public enum ErrorCode {

	VALIDATION_ERROR, // 入力値やリクエストデータの検証エラー
	BAD_REQUEST, // リクエスト形式が不正
	UNAUTHORIZED, // 認証が必要だが未認証
	FORBIDDEN, // 認証済みだが権限が不足
	NOT_FOUND, // 指定されたリソースが存在しない
	CONFLICT, // リソースの競合
	METHOD_NOT_ALLOWED, // サポートされていないHTTPメソッドでリクエストされた
	UNSUPPORTED_MEDIA_TYPE, // サポートされていないリクエスト形式
	INTERNAL_ERROR // サーバー内部エラー
}
