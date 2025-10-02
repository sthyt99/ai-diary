package com.example.ai_diary.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;
import com.example.ai_diary.backend.exception.ErrorMessages;
import com.example.ai_diary.backend.repository.DiaryRepository;

/**
 * 日記サービス
 */
@Service
public class DiaryService {

	private final DiaryRepository diaryRepository;

	public DiaryService(DiaryRepository diaryRepository) {
		this.diaryRepository = diaryRepository;
	}

	/**
	 * 日記作成処理
	 */
	public Diary create(Long userId, String content, Visibility visibility) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException(ErrorMessages.DIARY_CONTENT_EMPTY);
		}
		if (userId == null) {
			throw new IllegalArgumentException(ErrorMessages.USER_ID_REQUIRED);
		}

		Diary d = new Diary();
		d.setUserId(userId);
		d.setContent(content);
		d.setVisibility(visibility != null ? visibility : Visibility.PRIVATE);
		return diaryRepository.save(d);
	}

	/**
	 * 日記取得処理
	 * 公開設定された日記 (Visibility.PUBLIC) を
	 * 作成日時の新しい順 (CreatedAt の降順) で
	 * ページング付きで取得する
	 */
	public Page<Diary> publicFeed(int page, int size) {
		PageRequest pageable = PageRequest.of(page, size);
		return diaryRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC, pageable);
	}
}
