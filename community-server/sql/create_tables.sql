-- Create communities table
CREATE TABLE IF NOT EXISTS public.communities (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name text NOT NULL,
  description text,
  owner_id uuid NOT NULL,
  is_public boolean DEFAULT TRUE,
  created_at timestamptz DEFAULT now()
);

-- Create messages table
CREATE TABLE IF NOT EXISTS public.messages (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  community_id uuid REFERENCES public.communities(id) ON DELETE CASCADE,
  user_id uuid NOT NULL,
  content text NOT NULL,
  type text DEFAULT 'text',
  client_message_id text,
  created_at timestamptz DEFAULT now()
);

-- Optional: index for fast queries
CREATE INDEX IF NOT EXISTS idx_messages_community_created ON public.messages (community_id, created_at DESC);

-- Optional: unique index on client_message_id to help deduping optimistic messages
CREATE UNIQUE INDEX IF NOT EXISTS idx_messages_client_message_id ON public.messages (client_message_id);

-- Example RLS policies (start conservative). Use server service_role to write directly.
-- Enable RLS if desired and add policies accordingly.
-- NOTE: The server uses service_role to bypass RLS for writes; client-only writes should have strict policies.
