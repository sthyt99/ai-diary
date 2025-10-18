package com.example.ai_diary.backend.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI の呼び出しクライアント
 */
@Component
public class OpenAiClient {
	
	private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

	private final String apiKey;
	private final String apiUrl;
	private final String model;
	private final int timeoutMs;
	private final HttpClient http;
	
	/** JavaのオブジェクトをJSON文字列に相互変換するためインスタンス作成 */
	@Autowired
	private ObjectMapper mapper;

	public OpenAiClient(
			
			/** application.properties の値を読み込む */
			@Value("${openai.api.key}") String apiKey,
			@Value("${openai.api.url}") String apiUrl,
			@Value("${openai.model:gpt-4o-mini}") String model,
			@Value("${openai.request.timeout-ms:10000}") int timeoutMs,
			ObjectMapper mapper) {

		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
		this.model = model;
		this.timeoutMs = timeoutMs;
		this.http = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(timeoutMs))
				.build();
		this.mapper = mapper;
	}

	/**
	 * Chat Completions API を叩いて最初の候補の content を返す
	 */
	public String chat(String systemPrompt, String userContent) throws Exception {

		String payload = """
				{
				          "model": "%s",
				          "temperature": 0.7,
				          "messages": [
				            {"role":"system","content": %s},
				            {"role":"user","content": %s}
				          ]
				}
				""".formatted(model,
				mapper.writeValueAsString(systemPrompt),
				mapper.writeValueAsString(userContent));

		HttpRequest req = HttpRequest.newBuilder(URI.create(apiUrl))
				.timeout(Duration.ofMillis(timeoutMs))
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
				.build();

		HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		
		if (res.statusCode() == 429) {
			
			// 詳細ログ
			log.warn("OpenAI quota exceeded: {}", truncate(res.body(), 400));
			
		    throw new OpenAiQuotaExceededException("OpenAI quota exceeded");
		}
		
		if (res.statusCode() < 200 || res.statusCode() >= 300) {
			
			// 詳細ログ
            log.warn("OpenAI API error status={} body={}", res.statusCode(), truncate(res.body(), 400));
			
			throw new IllegalStateException("OpenAi API error!: " + res.statusCode() + " " + res.body());
		}

		JsonNode root = mapper.readTree(res.body());
		JsonNode choices = root.path("choices");

		if (!choices.isArray() || choices.isEmpty()) {
			throw new IllegalStateException("OpenAI response has no choices.");
		}

		return choices.get(0).path("message").path("content").asText();
	}
	
	private static String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "...";
    }
}
