package com.example.ai_diary.backend.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AiTransformService {

	private final OpenAiClient client;
	@Autowired
	private final ObjectMapper mapper;
	
	/** AI生成のON・OFFスイッチ */
	private final boolean enabled;

	public AiTransformService(OpenAiClient client, ObjectMapper mapper, @Value("${ai.enabled:true}") boolean enabled) {
		this.client = client;
		this.mapper = mapper;
		this.enabled = enabled;
	}

	/**
	* 指定スタイル（0..n）で content を変換し、JSON文字列を返す。
	* 例: {"summary":"...", "haiku":"...", "quote":"..."}
	*/
	public String transformToJson(String content, List<String> styles) {

		if (!enabled || styles == null || styles.isEmpty())
			return null;

		// 空のJSONオブジェクトを作成する
		ObjectNode root = mapper.createObjectNode();

		for (String s : styles) {

			AiStyle st;

			try {
				st = AiStyle.from(s);
			} catch (Exception e) {

				// 不正な値はスキップ
				continue;
			}

			try {

				// OpenAIへ送信する文章を作成する
				String out = client.chat(st.prompt(),
						"本文:\n" + content.trim());

				switch (st) {

				case SUMMARY -> root.put("summary", out);
				case HAIKU -> root.put("haiku", out.replaceAll("\\r?\\n", " / "));
				case QUOTE -> root.put("quote", out);
				}
				
			} catch (OpenAiQuotaExceededException q) {
				
				// クォータ枯渇時はnull返却
				return null;
				
			} catch (Exception apiEx) {

				// 個別失敗は握りつぶして続行（他スタイルは生成する）
				root.put(st.name().toLowerCase(), (String) null);
			}
		}

		return root.isEmpty() ? null : root.toString();
	}
}
