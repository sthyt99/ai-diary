package com.example.ai_diary.backend.exception;

/**
 * エラーメッセージ定数
 */
public class ErrorMessages {
	
	private ErrorMessages() {
		// インスタンス化禁止
	}

	// 400: Validation / Bad Request
	public static final String VALIDATION_INPUT_ERROR = "入力に誤りがあります。";
	public static final String VALIDATION_PARAM_ERROR = "パラメータに誤りがあります。";
	public static final String BAD_REQUEST_MISSING_PARAM = "必須パラメータが不足しています: ";
	public static final String BAD_REQUEST_NOT_READABLE = "リクエストボディの解析に失敗しました。";
	public static final String EMAIL_ALREADY_USED = "このメールアドレスは既に使用されています。";
	public static final String USER_NOT_FOUND = "ユーザーが見つかりません: ";
	public static final String INVALID_EMAIL = "メールアドレスが不正です: ";
	public static final String PASSWORD_REQUIRED = "パスワードは必須です";
    public static final String PASSWORD_TOO_WEAK = "パスワードは8文字以上にしてください";
    public static final String DISPLAY_NAME_REQUIRED = "表示名は必須です";
    // 日記関連
	public static final String DIARY_CONTENT_EMPTY = "日記の内容を入力してください。";
	public static final String USER_ID_REQUIRED    = "ユーザーIDは必須です。";
    public static final String DIARY_NOT_FOUND = "日記が見つかりません";
    public static final String DIARY_ACCESS_DENIED = "この日記を閲覧する権限がありません";
	
	// 401 / 403
	public static final String UNAUTHORIZED = "認証が必要です。";
	public static final String FORBIDDEN = "この操作を行う権限がありません。";

	// 404
	public static final String NOT_FOUND_RESOURCE = "リソースが見つかりません。";

	// 409
	public static final String CONFLICT_DATA_INTEGRITY = "データ整合性エラー（一意制約など）。";

	// 405 / 415
	public static final String METHOD_NOT_ALLOWED = "このHTTPメソッドは許可されていません。";
	public static final String UNSUPPORTED_MEDIA_TYPE = "このContent-Typeはサポートしていません。";

	// 500
	public static final String INTERNAL_SERVER_ERROR = "サーバ内部でエラーが発生しました。";
}
