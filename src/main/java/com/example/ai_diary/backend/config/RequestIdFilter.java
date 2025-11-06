package com.example.ai_diary.backend.config;

import java.io.IOException;
import java.util.UUID;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	/** ヘッダー */
	private static final String HEADER = "X-Request-Id";
	/** MDCキー */
	private static final String MDC_KEY = "requestId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String rid = request.getHeader(HEADER);

		if (rid == null || rid.isBlank()) {
			rid = UUID.randomUUID().toString();
		}

		MDC.put(MDC_KEY, rid);
		response.setHeader(HEADER, rid);

		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}
}
