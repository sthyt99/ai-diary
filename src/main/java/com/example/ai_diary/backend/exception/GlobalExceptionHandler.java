package com.example.ai_diary.backend.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.ai_diary.backend.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * グローバル例外ハンドラ
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String COLON = ": ";

	/** 400: バリデーション（@Valid） */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpServletRequest req) {

		List<String> details = new ArrayList<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(fe -> details.add(fe.getField() + COLON + fe.getDefaultMessage()));
		ex.getBindingResult().getGlobalErrors()
				.forEach(ge -> details.add(ge.getObjectName() + COLON + ge.getDefaultMessage()));

		return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
				ErrorMessages.VALIDATION_INPUT_ERROR, req.getRequestURI(), details);
	}

	/** 400: @Validated の ConstraintViolation（パス/クエリ） */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(
			ConstraintViolationException ex, HttpServletRequest req) {

		List<String> details = ex.getConstraintViolations().stream()
				.map(v -> v.getPropertyPath() + COLON + v.getMessage())
				.toList();

		return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
				ErrorMessages.VALIDATION_PARAM_ERROR, req.getRequestURI(), details);
	}

	/** 400: リクエスト不備（必須パラメータ欠如など） */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(
			MissingServletRequestParameterException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST,
				ErrorMessages.BAD_REQUEST_MISSING_PARAM + ex.getParameterName(),
				req.getRequestURI(), null);
	}

	/** 400: フォーマット・JSON解析エラー等 */
	@ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(
			org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST,
				ErrorMessages.BAD_REQUEST_NOT_READABLE, req.getRequestURI(), null);
	}

	/** 401: 認証失敗（ログイン失敗など） */
	@ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
	public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ErrorMessages.UNAUTHORIZED, req.getRequestURI(),
				null);
	}

	/** 403: 認可失敗（権限不足） */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
		return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, ErrorMessages.FORBIDDEN, req.getRequestURI(), null);
	}

	/** 404: データ見つからず */
	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNoSuchElement(
			NoSuchElementException ex, HttpServletRequest req) {
		return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND,
				ErrorMessages.NOT_FOUND_RESOURCE, req.getRequestURI(), null);
	}

	/** 409: 一意制約違反など */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(
			DataIntegrityViolationException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ErrorCode.CONFLICT,
				ErrorMessages.CONFLICT_DATA_INTEGRITY, req.getRequestURI(), null);
	}

	/** 405: メソッド不許可 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
			HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
		return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED,
				ErrorMessages.METHOD_NOT_ALLOWED, req.getRequestURI(), null);
	}

	/** 415: サポート外メディアタイプ */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
			HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
		return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.UNSUPPORTED_MEDIA_TYPE,
				ErrorMessages.UNSUPPORTED_MEDIA_TYPE, req.getRequestURI(), null);
	}

	/** 400: 業務ロジックの軽いエラー（例：メール重複） */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST,
				ex.getMessage(), req.getRequestURI(), null);
	}

	/** 500: 想定外 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(
			Exception ex, HttpServletRequest req) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
				ErrorMessages.INTERNAL_SERVER_ERROR, req.getRequestURI(), null);
	}

	/** 共通ビルダー */
	private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code,
			String message, String path, List<String> details) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code.name(),
				message,
				path,
				details);
		return ResponseEntity.status(status).body(body);
	}
}
