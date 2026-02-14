ALTER TABLE recipes
    ADD COLUMN IF NOT EXISTS owner_user_id VARCHAR(36);

CREATE INDEX IF NOT EXISTS idx_recipes_owner_user_id ON recipes(owner_user_id);
