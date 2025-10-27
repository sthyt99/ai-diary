package com.example.ai_diary.backend.ai;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * IP毎のレート制限を行うServletフィルタクラス
 */
@Component
public class RateLimitFilter implements Filter {

	/** 1秒あたりの最大リクエスト数 */
	private static final int LIMIT_PER_SEC = 10;
	/** IPアドレス毎のカウンタを保持する */
	private final Map<String, Window> counters = new ConcurrentHashMap<>();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;

		// クライアントのIPを取得する
		String ip = req.getRemoteAddr();

		// 現在時刻を秒単位で取得する
		long nowSec = Instant.now().getEpochSecond();

		// Windowが無ければ作成する
		Window w = counters.computeIfAbsent(ip, k -> new Window(nowSec));

		// 排他制御
		synchronized (w) {

			// 秒が変わったらカウンタをリセット
			if (w.second != nowSec) {
				w.second = nowSec;
				w.count.set(0);
			}

			// カウンタをインクリメントして現在値を取得し、上限を超えた場合「429」を返却する
			if (w.count.incrementAndGet() > LIMIT_PER_SEC) {
				HttpServletResponse res = (HttpServletResponse) response;
				res.setStatus(429);
				res.setContentType("application/json");
				res.getWriter().write("{\"error\":\"rate_limited\"}");
				return;
			}
		}

		// 次の処理へ
		chain.doFilter(request, response);
	}

	/**
	 * 1秒のカウントを持つクラス
	 */
	private static class Window {

		// カウント中の秒
		long second;

		// 秒の間に来たリクエスト数
		AtomicInteger count = new AtomicInteger(0);

		Window(long s) {
			this.second = s;
		}
	}
}
