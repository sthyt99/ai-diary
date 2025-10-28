package com.example.ai_diary.backend.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ai_diary.backend.ai.AiTransformService;
import com.example.ai_diary.backend.config.PagingProperties;
import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;
import com.example.ai_diary.backend.exception.ErrorMessages;
import com.example.ai_diary.backend.repository.DiaryRepository;
import com.example.ai_diary.backend.repository.UserRepository;

/**
 * 日記サービス
 */
@Service
public class DiaryService {

	private final DiaryRepository diaryRepository;
	private final UserRepository userRepository;
	private final AiTransformService aiTransformService;
	private final PagingProperties paging;

	public DiaryService(DiaryRepository diaryRepository, UserRepository userRepository,
			AiTransformService aiTransformService, PagingProperties paging) {
		this.diaryRepository = diaryRepository;
		this.userRepository = userRepository;
		this.aiTransformService = aiTransformService;
		this.paging = paging;
	}

	/**
	 * 日記作成処理
	 */
	@Transactional
	public Diary create(Long userId, String content, Visibility visibility, List<String> styles) {

		String normalized = (content == null) ? null : content.strip();

		if (normalized == null || normalized.isEmpty()) {
			throw new IllegalArgumentException(ErrorMessages.DIARY_CONTENT_EMPTY);
		}
		if (userId == null) {
			throw new IllegalArgumentException(ErrorMessages.USER_ID_REQUIRED);
		}

		userRepository.findById(userId)
				.orElseThrow(() -> new NoSuchElementException(ErrorMessages.USER_NOT_FOUND));

		Diary d = new Diary();
		d.setUserId(userId);
		d.setContent(normalized);
		d.setVisibility(visibility != null ? visibility : Visibility.PRIVATE);

		// AI生成
		try {
			String aiJson = aiTransformService.transformToJson(normalized, styles);
			d.setContentAi(aiJson);
		} catch (Exception ignore) {
			d.setContentAi(null);
		}

		return diaryRepository.save(d);
	}

	/**
	 * 公開フィード取得処理
	 * 公開設定された日記 (Visibility.PUBLIC) を
	 * 作成日時の新しい順 (CreatedAt の降順) で
	 * ページング付きで取得する
	 */
	@Transactional(readOnly = true)
	public Page<Diary> publicFeed(int page, int size) {
		if (page < 0)
			page = 0;
		
		int max = paging.getMaxSize();
		int def = paging.getDefaultSize();
		
		size = (size <= 0) ? def : Math.min(size, max);
		
		PageRequest pr = PageRequest.of(page, size);
		
		return diaryRepository.findByVisibilityOrderByCreatedAtDesc(Visibility.PUBLIC, pr);
	}

	/**
	* IDで日記を取得し、可視性をチェックする。
	* - PUBLIC: 誰でも閲覧可
	* - PRIVATE: 所有者のみ閲覧可
	* - FRIENDS: 将来的に友達機能があれば拡張
	*
	* @param diaryId 取得する日記のID
	* @param requesterId 閲覧を試みるユーザーのID（未ログイン時はnull）
	* @return Diary
	* @throws IllegalArgumentException アクセス権がない場合
	* @throws NoSuchElementException   日記が存在しない場合
	*/
	@Transactional(readOnly = true)
	public Diary getOneWithVisibilityCheck(Long diaryId, Long requesterId) {
		Diary diary = diaryRepository.findById(diaryId)
				.orElseThrow(() -> new NoSuchElementException(ErrorMessages.DIARY_NOT_FOUND));

		Visibility v = diary.getVisibility();

		switch (v) {
		case PUBLIC:
			return diary;
		case PRIVATE:
		case LIMITED:
			// 今は本人のみ扱い（友達限定を実装する際、判定追加）
			if (requesterId != null && requesterId.equals(diary.getUserId())) {
				return diary;
			}
			throw new AccessDeniedException(ErrorMessages.DIARY_ACCESS_DENIED);

		default:
			throw new AccessDeniedException(ErrorMessages.DIARY_ACCESS_DENIED);
		}
	}

	/**
	 * 日記を削除する
	 */
	@Transactional
	public void deleteOwn(Long diaryId, Long meUserId) {
		Diary d = diaryRepository.findById(diaryId)
				.orElseThrow(() -> new NoSuchElementException(ErrorMessages.DIARY_NOT_FOUND));
		if (!d.getUserId().equals(meUserId)) {
			throw new AccessDeniedException(ErrorMessages.FORBIDDEN);
		}
		diaryRepository.delete(d);
	}
}
