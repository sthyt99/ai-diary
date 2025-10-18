package com.example.ai_diary.backend.ai;

/**
 * AI生成スタイル
 * - 各スタイルに「キー名」と「システムプロンプト」を保持
 * - 不正入力は例外を出さず null を返す
 */
public enum AiStyle {

    SUMMARY("summary", 
        "以下の本文を日本語で3文以内に要約してください。箇条書き可。不要な脚色は禁止。"),

    HAIKU("haiku", 
        "以下の本文の情景を日本語の俳句（五七五）で1つ作成してください。季語は任意。句点不要。"),

    QUOTE("quote", 
        "以下の本文から1つの短い『名言風』の一文に変換してください。20字以内、ポジティブに。句読点は1つまで。");

    private final String key;            // 外部入力やAPIで使う識別子
    private final String systemPrompt;   // ChatGPT等に渡す指示文

    AiStyle(String key, String systemPrompt) {
        this.key = key;
        this.systemPrompt = systemPrompt;
    }

    /** APIや外部入力で使うキー */
    public String key() {
        return key;
    }

    /** システムプロンプトを返す */
    public String prompt() {
        return systemPrompt;
    }

    /**
     * 文字列からAiStyleを安全に取得
     * - nullや不正値ならnullを返す
     * - 大文字小文字や空白は無視
     */
    public static AiStyle from(String s) {
        if (s == null) return null;
        String normalized = s.trim().toLowerCase();
        for (AiStyle style : values()) {
            if (style.key.equals(normalized)) {
                return style;
            }
        }
        return null;
    }
}
