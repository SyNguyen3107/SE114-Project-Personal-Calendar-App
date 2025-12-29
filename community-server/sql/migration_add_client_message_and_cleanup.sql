-- Migration: add client_message_id column and partial unique index
-- Run as role: postgres in Supabase SQL editor

-- 1) Add column (if missing)
ALTER TABLE public.messages
  ADD COLUMN IF NOT EXISTS client_message_id text;

-- 2) Create a unique index only for non-null client_message_id values
CREATE UNIQUE INDEX IF NOT EXISTS idx_messages_client_message_id
ON public.messages (client_message_id)
WHERE client_message_id IS NOT NULL;

-- 3) Preview legacy 'reaction' and numeric '1' messages
SELECT id, community_id, user_id, content, created_at
FROM public.messages
WHERE content = '<reaction>' OR trim(content) = '1'
ORDER BY created_at DESC
LIMIT 200;

-- 4) Backup legacy rows (run only if you will delete afterwards)
CREATE TABLE IF NOT EXISTS public.messages_legacy_backup AS
SELECT * FROM public.messages WHERE content = '<reaction>' OR trim(content) = '1';

-- 5) When you confirm backup, delete the legacy rows
-- DELETE FROM public.messages WHERE content = '<reaction>' OR trim(content) = '1';

-- Note: Run step 1-3 first, inspect results, then run step 4 and finally uncomment and run step 5 to delete.
