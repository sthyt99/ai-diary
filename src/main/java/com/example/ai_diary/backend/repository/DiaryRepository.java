package com.example.ai_diary.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

	Page<Diary> findByVisibilityOrderByCreatedAtDesc(Visibility visibility, Pageable pageable);

	Optional<Diary> findById(Long id);
}
