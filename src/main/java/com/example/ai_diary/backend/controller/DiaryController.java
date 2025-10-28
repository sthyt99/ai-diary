package com.example.ai_diary.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;
import com.example.ai_diary.backend.dto.DiaryDtos.CreateRequest;
import com.example.ai_diary.backend.dto.DiaryDtos.Response;
import com.example.ai_diary.backend.repository.DiaryRepository;
import com.example.ai_diary.backend.repository.UserRepository;
import com.example.ai_diary.backend.service.DiaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * 日記コントローラ
 */
@RestController
public class DiaryController {

	private final DiaryService diaryService;
	private final DiaryRepository diaryRepository;
	private final UserRepository userRepository;

	public DiaryController(DiaryService diaryService, DiaryRepository diaryRepository, UserRepository userRepository) {
		this.diaryService = diaryService;
		this.diaryRepository = diaryRepository;
		this.userRepository = userRepository;
	}

	@Operation(summary="日記作成", description="AI生成はstyles指定時のみ。JWT必須。")
	@SecurityRequirement(name = "bearerAuth")
	@PostMapping("/api/diary")
	public ResponseEntity<Response> create(@Valid @RequestBody CreateRequest req,
			@AuthenticationPrincipal UserDetails principal) {

		Long userId = userRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
		Diary diary = diaryService.create(userId, req.getContent(), req.getVisibility(), req.getStyles());
		Response res = toResponse(diary);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/api/diary/{id}")
	public ResponseEntity<Response> getOne(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
		Diary diary = diaryRepository.findById(id).orElseThrow();

		// PUBLIC は全員、PRIVATE/LIMITED は本人のみ
		if (diary.getVisibility() != Visibility.PUBLIC) {
			if (principal == null)
				return ResponseEntity.status(403).build();
			Long me = userRepository.findByEmail(principal.getUsername()).orElseThrow().getId();
			if (!diary.getUserId().equals(me))
				return ResponseEntity.status(403).build();
		}

		return ResponseEntity.ok(toResponse(diary));
	}

	@GetMapping("/api/feed")
	public Page<Response> feed(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		
		return diaryService.publicFeed(page, size).map(this::toResponse);
	}

	/**
	 * 日記情報DTO取得
	 */
	private Response toResponse(Diary d) {
		Response r = new Response();
		r.setId(d.getId());
		r.setUserId(d.getUserId());
		r.setContent(d.getContent());
		r.setContentAi(d.getContentAi());
		r.setVisibility(d.getVisibility());
		r.setCreatedAt(d.getCreatedAt());
		return r;
	}
}
