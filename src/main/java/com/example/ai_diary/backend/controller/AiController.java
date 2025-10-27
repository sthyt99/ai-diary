package com.example.ai_diary.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_diary.backend.ai.AiTransformService;

/**
 * AIが使用できるか確認
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

	private final AiTransformService svc;
	public AiController(AiTransformService svc) {
		this.svc = svc;
	}
	
	@GetMapping("/ping")
	public ResponseEntity<?> ping() {
		String res = svc.transformToJson("疎通テスト", List.of("SUMMARY"));
		return ResponseEntity.ok(res == null ? "{\"status\":\"AI_DISABLED_OR_ERROR\"}" : res);
	}
}
