package com.example.ai_diary.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ai_diary.backend.domain.User;
import com.example.ai_diary.backend.repository.UserRepository;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder encoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, encoder);
    }

    @Test
    void signup_success_hashAndNormalize() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User u = userService.signup(" Test@Example.com ", "Passw0rd!", "taro");

        assertNotNull(u.getId());
        assertEquals("test@example.com", u.getEmail(), "メールは小文字化される");
        assertTrue(encoder.matches("Passw0rd!", u.getPasswordHash()), "ハッシュ化される");

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        assertEquals("taro", cap.getValue().getDisplayName());
    }

    @Test
    void signup_duplicateEmail_callsExistsByEmail_withNormalizedAddress_andNoSave() {
        // Arrange
        // 何が来ても重複 true を返す（分岐に入れるため）
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // わざと揺らぎのある入力（前後空白、全角、大小混在）
        String raw = "  Ｄｕｐ＠Ｅｘａｍｐｌｅ．ｃｏｍ  ";

        // Act + Assert (例外が投げられること)
        assertThrows(IllegalArgumentException.class,
                () -> userService.signup(raw, "password123", "name"));

        // Verify 1: 正規化後の文字列で existsByEmail が呼ばれていること
        //   ※ あなたの EmailNormalizer 実装に合わせて期待値を記述
        verify(userRepository).existsByEmail("dup@example.com");

        // Verify 2: save は呼ばれていないこと（重複検知で終了）
        verify(userRepository, never()).save(any());

        // （任意）呼び出し順を厳密に見るなら
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByEmail("dup@example.com");
        inOrder.verifyNoMoreInteractions();

        // （任意）引数キャプチャで“本当にその値だったか”を明示チェック
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).existsByEmail(emailCaptor.capture());
        String actual = emailCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("dup@example.com", actual);
    }

    @Test
    void signup_missingFields_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> userService.signup(null, "x", "y"));
        assertThrows(IllegalArgumentException.class, () -> userService.signup("a@b", null, "y"));
        assertThrows(IllegalArgumentException.class, () -> userService.signup("a@b", "x", null));
    }

    @Test
    void findByEmailOrThrow_found() {
        User u = new User();
        u.setId(9L); u.setEmail("a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        User got = userService.findByEmailOrThrow("a@b.com");
        assertEquals(9L, got.getId());
    }

    @Test
    void findByEmailOrThrow_notFound_throwsNoSuchElement() {
        when(userRepository.findByEmail("none@b.com")).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class,
                () -> userService.findByEmailOrThrow("none@b.com"));
    }
}
