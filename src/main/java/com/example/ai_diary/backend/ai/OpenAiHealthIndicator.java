package com.example.ai_diary.backend.ai;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * HealthIndicatorインターフェースを用いて、
 * OpenAiの健康状態（正常な挙動か）を確認
 */
@Component
public class OpenAiHealthIndicator implements HealthIndicator {

	private final OpenAiClient client;

	public OpenAiHealthIndicator(OpenAiClient client) {
		this.client = client;
	}

	@Override
	public Health health() {

		try {
			client.chat("Reply with OK.", "OK");

			// 正常な場合、withDetail()の情報でオブジェクトを作成
			return Health.up().withDetail("openai", "UP").build();

		} catch (OpenAiQuotaExceededException e) {
			return Health.down().withDetail("openai", "OpenAI quota exceeded").build();
		} catch (Exception e) {
			return Health.down().withDetail("openai", "DOWN").withDetail("reason", e.getClass().getSimpleName())
					.build();
		}
	}
}
