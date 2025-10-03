package com.example.ai_diary.backend.util;

import java.text.Normalizer;

/**
 * メールアドレスの正規化
 */
public final class EmailNormalizer {

	private EmailNormalizer() {
	}

	/** null安全・前後空白除去・NFKC正規化・小文字化 */
	public static String normalize(String raw) {
		if (raw == null)
			return null;
		String trimmed = raw.trim();
		String nfkc = Normalizer.normalize(trimmed, Normalizer.Form.NFKC);
		return nfkc.toLowerCase();
	}
}
