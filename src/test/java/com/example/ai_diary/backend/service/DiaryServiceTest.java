package com.example.ai_diary.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.ai_diary.backend.ai.AiTransformService;
import com.example.ai_diary.backend.config.PagingProperties;
import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;
import com.example.ai_diary.backend.repository.DiaryRepository;
import com.example.ai_diary.backend.repository.UserRepository;

class DiaryServiceTest {

    private DiaryRepository diaryRepository;
    private UserRepository userRepository;
    private AiTransformService aiTransformService;
    private DiaryService diaryService;
    private PagingProperties paging;

    @BeforeEach
    void setUp() {
        diaryRepository = mock(DiaryRepository.class);
        userRepository = mock(UserRepository.class);
        aiTransformService = mock(AiTransformService.class);
        diaryService = new DiaryService(diaryRepository, userRepository, aiTransformService, paging);
    }

    @Test
    void create_success_setsContentAi_andDoesNotOverwriteContent() {
        // arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(new com.example.ai_diary.backend.domain.User()));
        when(aiTransformService.transformToJson(anyString(), anyList())).thenReturn("{\"summary\":\"要約\"}");
        when(diaryRepository.save(any(Diary.class))).thenAnswer(inv -> {
            Diary d = inv.getArgument(0);
            d.setId(100L);
            return d;
        });

        // act
        Diary d = diaryService.create(1L, "  今日は散歩した  ", Visibility.PUBLIC, List.of("SUMMARY","HAIKU"));

        // assert
        assertEquals(100L, d.getId());
        assertEquals("今日は散歩した", d.getContent(), "本文はtrimのみで上書きされない");
        assertEquals(Visibility.PUBLIC, d.getVisibility());
        assertNotNull(d.getContentAi(), "AI生成結果が格納される");
        assertTrue(d.getContentAi().contains("summary"));

        // 保存時のエンティティを確認
        ArgumentCaptor<Diary> cap = ArgumentCaptor.forClass(Diary.class);
        verify(diaryRepository).save(cap.capture());
        Diary saved = cap.getValue();
        assertEquals("今日は散歩した", saved.getContent());
        assertEquals(d.getContentAi(), saved.getContentAi());
    }

    @Test
    void create_aiFailure_stillSaves_withContentAiNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new com.example.ai_diary.backend.domain.User()));
        when(aiTransformService.transformToJson(anyString(), anyList())).thenThrow(new RuntimeException("timeout"));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(inv -> inv.getArgument(0));

        Diary d = diaryService.create(1L, "本文", Visibility.PRIVATE, List.of("SUMMARY"));

        assertEquals("本文", d.getContent());
        assertNull(d.getContentAi(), "AI失敗時はnull");
        verify(diaryRepository, times(1)).save(any(Diary.class));
    }

    @Test
    void create_missingContent_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> diaryService.create(1L, "   ", Visibility.PUBLIC, List.of("SUMMARY")));
        assertThrows(IllegalArgumentException.class,
                () -> diaryService.create(1L, null, Visibility.PUBLIC, List.of("SUMMARY")));
    }

    @Test
    void create_missingUserId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> diaryService.create(null, "本文", Visibility.PUBLIC, List.of("SUMMARY")));
    }

    @Test
    void create_userNotFound_throwsNoSuchElement() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> diaryService.create(9L, "本文", Visibility.PUBLIC, List.of("SUMMARY")));
    }

    @Test
    void publicFeed_respectsMaxPageSize50_andUsesPublicVisibility() {
        // arrange
        when(diaryRepository.findByVisibilityOrderByCreatedAtDesc(eq(Visibility.PUBLIC), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Diary())));

        // act
        diaryService.publicFeed(2, 100); // size=100 を要求 → 50 に丸める仕様

        // assert: 渡された Pageable を検証
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(diaryRepository).findByVisibilityOrderByCreatedAtDesc(eq(Visibility.PUBLIC), cap.capture());
        Pageable pg = cap.getValue();
        assertEquals(2, pg.getPageNumber());
        assertEquals(50, pg.getPageSize(), "サイズは上限50に丸められる");
    }

    @Test
    void getOneWithVisibilityCheck_public_anyoneOk() {
        Diary d = new Diary();
        d.setId(10L); d.setUserId(1L); d.setVisibility(Visibility.PUBLIC);
        when(diaryRepository.findById(10L)).thenReturn(Optional.of(d));

        Diary got = diaryService.getOneWithVisibilityCheck(10L, null);
        assertEquals(10L, got.getId());
    }

    @Test
    void getOneWithVisibilityCheck_private_ownerOnly() {
        Diary d = new Diary();
        d.setId(11L); d.setUserId(99L); d.setVisibility(Visibility.PRIVATE);
        when(diaryRepository.findById(11L)).thenReturn(Optional.of(d));

        // 所有者 → OK
        assertEquals(11L, diaryService.getOneWithVisibilityCheck(11L, 99L).getId());
        // 他人 → 403
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> diaryService.getOneWithVisibilityCheck(11L, 100L));
        // 未ログイン → 403
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> diaryService.getOneWithVisibilityCheck(11L, null));
    }

    @Test
    void getOneWithVisibilityCheck_limited_nowSameAsOwnerOnly() {
        Diary d = new Diary();
        d.setId(12L); d.setUserId(7L); d.setVisibility(Visibility.LIMITED);
        when(diaryRepository.findById(12L)).thenReturn(Optional.of(d));

        assertEquals(12L, diaryService.getOneWithVisibilityCheck(12L, 7L).getId());
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> diaryService.getOneWithVisibilityCheck(12L, 8L));
    }

    @Test
    void getOneWithVisibilityCheck_notFound_throws404() {
        when(diaryRepository.findById(13L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> diaryService.getOneWithVisibilityCheck(13L, 1L));
    }
}
