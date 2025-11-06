package com.example.ai_diary.backend.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.stream.IntStream;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.ai_diary.backend.domain.Diary;
import com.example.ai_diary.backend.domain.Visibility;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DiaryRepositoryTest {

    // --- Testcontainers PostgreSQL ---
    @Container
    @ServiceConnection // Spring Boot が DataSource を自動生成して接続
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DiaryRepository diaryRepository;
    
    /** Flywayを実行 */
    @BeforeAll
    static void migrate() {
        Flyway.configure()
              .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
              .locations("classpath:db/migration")
              .load()
              .migrate();
    }

    @BeforeAll
    static void setup(@Autowired JdbcTemplate jdbc) {
        // Flyway が自動実行されるため、最低限のユーザーだけ作成
        jdbc.update("""
            INSERT INTO users (email, password_hash, display_name, premium_flag, created_at)
            VALUES ('u1@test.local', 'aitest', 'u1', false, NOW())
        """);
    }

    @Test
    void pagingTest() {
        long u1 = jdbc.queryForObject("SELECT id FROM users WHERE email='u1@test.local'", Long.class);

        // Arrange: いくつかデータ投入
        IntStream.rangeClosed(1, 10).forEach(i -> {
            Diary d = new Diary();
            d.setUserId(u1);
            d.setVisibility((i % 2 == 0) ? Visibility.PUBLIC : Visibility.PRIVATE);
            d.setContent("entry-" + i);
            d.setCreatedAt(Instant.now().minusSeconds(60L * i));
            diaryRepository.save(d);
        });

        // Act
        var publicPage = diaryRepository.findByVisibilityOrderByCreatedAtDesc(
                Visibility.PUBLIC, org.springframework.data.domain.PageRequest.of(0, 5));

        // Assert
        assertThat(publicPage.getContent()).allMatch(d -> d.getVisibility() == Visibility.PUBLIC);
        assertThat(publicPage.getContent().size()).isLessThanOrEqualTo(5);
    }
}
