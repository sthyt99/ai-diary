package com.example.ai_diary.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.domain.Visibility;
import com.example.ai_diary.backend.repository.DiaryRepository;
import com.example.ai_diary.backend.repository.UserRepository;

class DiaryServiceTest {

    private DiaryRepository diaryRepository;
    private UserRepository userRepository;
    private DiaryService diaryService;

    @BeforeEach
    void setUp() {
        diaryRepository = mock(DiaryRepository.class);
        userRepository = mock(UserRepository.class);
        diaryService = new DiaryService(diaryRepository, userRepository);
    }

    @Test
    void create_success_privateByDefault_andUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(diaryRepository.save(any(Diary.class))).thenAnswer(inv -> {
            Diary d = inv.getArgument(0);
            d.setId(10L);
            return d;
        });

        Diary d = diaryService.create(1L, " hello ", null);

        assertEquals(10L, d.getId());
        assertEquals(1L, d.getUserId());
        assertEquals("hello", d.getContent());
        assertEquals(Visibility.PRIVATE, d.getVisibility());
    }

    @Test
    void create_missingContent_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> diaryService.create(1L, "   ", Visibility.PUBLIC));
    }

    @Test
    void create_userNotFound_throwsNoSuchElement() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> diaryService.create(2L, "x", Visibility.PUBLIC));
    }

    @Test
    void publicFeed_returnsPage() {
        Page<Diary> page = new PageImpl<>(List.of(new Diary()));
        when(diaryRepository.findByVisibilityOrderByCreatedAtDesc(eq(Visibility.PUBLIC), any(Pageable.class)))
                .thenReturn(page);

        Page<Diary> got = diaryService.publicFeed(0,20);
        assertEquals(1, got.getTotalElements());
    }

    @Test
    void getOneWithVisibilityCheck_public_ok() {
        Diary d = new Diary();
        d.setId(5L); d.setUserId(1L); d.setVisibility(Visibility.PUBLIC);
        when(diaryRepository.findById(5L)).thenReturn(Optional.of(d));

        Diary got = diaryService.getOneWithVisibilityCheck(5L, null);
        assertEquals(5L, got.getId());
    }

    @Test
    void getOneWithVisibilityCheck_private_owner_ok() {
        Diary d = new Diary();
        d.setId(6L); d.setUserId(99L); d.setVisibility(Visibility.PRIVATE);
        when(diaryRepository.findById(6L)).thenReturn(Optional.of(d));

        Diary got = diaryService.getOneWithVisibilityCheck(6L, 99L);
        assertEquals(6L, got.getId());
    }

    @Test
    void getOneWithVisibilityCheck_private_other_forbidden() {
        Diary d = new Diary();
        d.setId(7L); d.setUserId(100L); d.setVisibility(Visibility.PRIVATE);
        when(diaryRepository.findById(7L)).thenReturn(Optional.of(d));

        assertThrows(AccessDeniedException.class,
                () -> diaryService.getOneWithVisibilityCheck(7L, 101L));
    }

    @Test
    void getOneWithVisibilityCheck_notFound_404() {
        when(diaryRepository.findById(8L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class,
                () -> diaryService.getOneWithVisibilityCheck(8L, 1L));
    }
}
