# Community Socket Server

This is a small Socket.IO server used by the SE114 Community feature.

Setup

1. Copy `.env.example` to `.env` and fill `SUPABASE_URL` and `SUPABASE_SERVICE_KEY`.
2. `npm install`
3. `npm run dev` to run with nodemon.

Security note

- Server uses Supabase service_role key to persist messages. Keep it secret.
- Clients must send a valid Supabase access token (JWT) either in `socket.handshake.auth.token` or as `?token=` query parameter.

SQL / Supabase setup

- Run `sql/create_tables.sql` in the Supabase SQL editor to create `communities` and `messages` tables.
- For write security, use the server to write messages/communities using the service_role key. If you want clients to write directly, create proper RLS policies (e.g., only allow insert when `auth.uid() = new.user_id`).

Endpoints & Socket events

- GET `/` - health
- Socket events (client -> server)

  - `join_community` { communityId }
  - `leave_community` { communityId }
  - `send_message` { communityId, content, type }

- Server emits
  - `message` { communityId, content, user, created_at }
  - `user_joined`, `user_left`
