package com.example.ai_diary.backend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** 認証ヘッダー */
	public static final String AUTH_HEADER = "Authorization";
	/** トークン先頭文字 */
	public static final String TOKEN_PREFIX = "Bearer ";
	/** トークン先頭文字長 */
	public static final int TOKEN_PREFIX_LENGTH = TOKEN_PREFIX.length();

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService uds) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = uds;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader(AUTH_HEADER);
		String token = null;

		if (StringUtils.hasText(header) && header.startsWith(TOKEN_PREFIX)) {
			token = header.substring(TOKEN_PREFIX_LENGTH);
		}

		if (token != null && jwtUtil.validate(token)
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			String email = jwtUtil.extractSubject(token);
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			var authorities = userDetails.getAuthorities();

			// 認証済みトークンを生成
			var auth = UsernamePasswordAuthenticationToken.authenticated(
					userDetails,
					null,
					authorities);

			// リクエスト情報を付加
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			// セキュリティコンテキストへ設定
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		chain.doFilter(request, response);
	}
}
