
-- Fix constraints and add missing columns if needed

-- Add title column to notes if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_schema='public' AND table_name='notes' AND column_name='title') THEN
        ALTER TABLE notes ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT 'Untitled';
    END IF;
END $$;

-- Rename raw_text to content if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
              WHERE table_schema='public' AND table_name='notes' AND column_name='raw_text') THEN
        ALTER TABLE notes RENAME COLUMN raw_text TO content;
    END IF;
END $$;

-- Update role constraint to match current enum values
DO $$
BEGIN
    -- Drop existing constraint if it exists
    ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
    
    -- Add new constraint
    ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('ADMIN', 'AGENT'));
END $$;
