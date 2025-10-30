package com.example.ai_diary.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.ai_diary.backend.security.CustomUserDetailsService;
import com.example.ai_diary.backend.security.JwtAuthenticationFilter;
import com.example.ai_diary.backend.security.JwtUtil;

@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(@Lazy JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
	
	@Bean
    @DependsOn("entityManagerFactory") // EMF完成後に生成
    public JwtAuthenticationFilter jwtAuthenticationFilter(@Lazy CustomUserDetailsService uds, JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil, uds);
    }

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.headers(h -> h.frameOptions(f -> f.disable()))
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/api/auth/**", "/actuator/**", "/h2-console/**").permitAll()
					.anyRequest().authenticated())
		.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}
}
