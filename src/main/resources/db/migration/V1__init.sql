-- === USERS ================================================================
CREATE TABLE IF NOT EXISTS users (
  id             BIGSERIAL PRIMARY KEY,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  display_name   VARCHAR(64),
  icon_url       VARCHAR(1024),
  premium_flag   BOOLEAN NOT NULL DEFAULT FALSE,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- === DIARIES ==============================================================
-- visibility はまずは VARCHAR + CHECK（ENUMは後でV2で移行しやすい）
CREATE TABLE IF NOT EXISTS diaries (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  visibility   VARCHAR(16) NOT NULL DEFAULT 'PRIVATE',
  content      TEXT NOT NULL,
  content_ai   JSON,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_diaries_visibility
    CHECK (visibility IN ('PRIVATE','LIMITED','PUBLIC'))
);

-- フィード/ユーザー別取得で効く索引
CREATE INDEX IF NOT EXISTS idx_diaries_visibility_created_at
  ON diaries (visibility, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_diaries_user_created_at
  ON diaries (user_id, created_at DESC);

-- === LIKES ================================================================
CREATE TABLE IF NOT EXISTS likes (
  id          BIGSERIAL PRIMARY KEY,
  diary_id    BIGINT NOT NULL REFERENCES diaries(id) ON DELETE CASCADE,
  user_id     BIGINT NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_likes UNIQUE (diary_id, user_id)
);

-- いいね一覧/重複チェックの実行計画を助ける索引
CREATE INDEX IF NOT EXISTS idx_likes_diary_created_at
  ON likes (diary_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_likes_user_created_at
  ON likes (user_id, created_at DESC);

-- === COMMENTS =============================================================
CREATE TABLE IF NOT EXISTS comments (
  id                 BIGSERIAL PRIMARY KEY,
  diary_id           BIGINT NOT NULL REFERENCES diaries(id) ON DELETE CASCADE,
  user_id            BIGINT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
  body               TEXT NOT NULL,
  parent_comment_id  BIGINT,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_comments_parent
    FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE SET NULL
);

-- ツリー表示/スレッド取得で効く索引
CREATE INDEX IF NOT EXISTS idx_comments_diary_created_at
  ON comments (diary_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_parent_created_at
  ON comments (parent_comment_id, created_at DESC);
