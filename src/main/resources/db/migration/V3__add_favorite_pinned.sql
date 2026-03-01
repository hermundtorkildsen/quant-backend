-- ============================
-- Add favorite support
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS is_favorite boolean;

UPDATE recipes
SET is_favorite = false
WHERE is_favorite IS NULL;

ALTER TABLE recipes
    ALTER COLUMN is_favorite SET DEFAULT false;

ALTER TABLE recipes
    ALTER COLUMN is_favorite SET NOT NULL;


-- ============================
-- Favorited timestamp
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS favorited_at timestamp;


-- ============================
-- Add pinned support
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS is_pinned boolean;

UPDATE recipes
SET is_pinned = false
WHERE is_pinned IS NULL;

ALTER TABLE recipes
    ALTER COLUMN is_pinned SET DEFAULT false;

ALTER TABLE recipes
    ALTER COLUMN is_pinned SET NOT NULL;


-- ============================
-- Pinned timestamp
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS pinned_at timestamp;


-- ============================
-- Last viewed
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS last_viewed_at timestamp;


-- ============================
-- View count
-- ============================

ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS view_count integer;

UPDATE recipes
SET view_count = 0
WHERE view_count IS NULL;

ALTER TABLE recipes
    ALTER COLUMN view_count SET DEFAULT 0;

ALTER TABLE recipes
    ALTER COLUMN view_count SET NOT NULL;