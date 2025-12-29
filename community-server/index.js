require("dotenv").config();
const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const fetch = require("node-fetch");

const app = express();
const server = http.createServer(app);

const PORT = process.env.PORT || 3000;
const SUPABASE_URL = process.env.SUPABASE_URL;
const SUPABASE_SERVICE_KEY = process.env.SUPABASE_SERVICE_KEY;

if (!SUPABASE_URL || !SUPABASE_SERVICE_KEY) {
  console.error("Missing SUPABASE_URL or SUPABASE_SERVICE_KEY in env");
  process.exit(1);
}

const io = new Server(server, {
  cors: {
    origin: process.env.CORS_ORIGIN || "*",
    methods: ["GET", "POST"],
  },
});

// Verify Supabase JWT token by calling /auth/v1/user
async function verifyToken(token) {
  if (!token) return null;
  try {
    console.log(
      "verifyToken: calling Supabase /auth/v1/user with token length=",
      token.length
    );
    const res = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
      headers: {
        Authorization: `Bearer ${token}`,
        apikey: SUPABASE_SERVICE_KEY,
      },
    });
    console.log("verifyToken: Supabase response status=", res.status);
    if (!res.ok) {
      const errText = await res.text();
      console.error(
        "verifyToken: Supabase rejected token:",
        res.status,
        errText
      );
      return null;
    }
    const json = await res.json();
    console.log("verifyToken: success, user id=", json.id);
    return json; // user object
  } catch (e) {
    console.error("verifyToken error", e);
    return null;
  }
}

// minimal health
app.get("/", (req, res) => res.send("Community socket server"));

io.use(async (socket, next) => {
  try {
    // try auth.token (modern) then query token (compat with Android client)
    let token = null;
    const hasAuthToken =
      socket.handshake && socket.handshake.auth && socket.handshake.auth.token;
    const hasQueryToken =
      socket.handshake &&
      socket.handshake.query &&
      socket.handshake.query.token;
    if (hasAuthToken) token = socket.handshake.auth.token;
    if (!token && hasQueryToken) token = socket.handshake.query.token;

    console.log("io.use handshake", {
      id: socket.id,
      hasAuthToken,
      hasQueryToken,
      queryTokenPreview: hasQueryToken
        ? String(socket.handshake.query.token).slice(0, 6) + "..."
        : null,
    });

    if (!token) {
      console.warn("Missing token in handshake for socket", socket.id);
      return next(new Error("Missing token"));
    }

    const user = await verifyToken(token);
    if (!user) {
      console.warn("Unauthorized token for socket", socket.id);
      return next(new Error("Unauthorized"));
    }
    socket.user = user;
    return next();
  } catch (e) {
    console.error("Handshake verify error", e);
    return next(new Error("Server error"));
  }
});

io.on("connection", (socket) => {
  console.log("connected", socket.user?.id);

  socket.on("join_community", ({ communityId }) => {
    if (!communityId) return;
    socket.join(`community:${communityId}`);
    io.to(`community:${communityId}`).emit("user_joined", {
      userId: socket.user.id,
    });
  });

  socket.on("leave_community", ({ communityId }) => {
    if (!communityId) return;
    socket.leave(`community:${communityId}`);
    io.to(`community:${communityId}`).emit("user_left", {
      userId: socket.user.id,
    });
  });

  socket.on("send_message", async (payload, callback) => {
    // payload: { communityId, content, type, client_message_id }
    const communityId = payload && payload.communityId;
    const content = payload && payload.content;
    const type = (payload && payload.type) || "text";
    const clientMessageId = payload && payload.client_message_id;

    console.log(
      "send_message received",
      communityId,
      "from",
      socket.user.id,
      "client_id=",
      clientMessageId
    );

    if (!communityId || !content) {
      if (callback) callback({ status: "error", message: "Invalid payload" });
      return;
    }

    // Reject legacy reaction placeholder sent as a message
    if (typeof content === "string" && content.trim() === "<reaction>") {
      console.warn(
        "Rejecting legacy reaction message save attempt from",
        socket.user.id
      );
      if (callback) callback({ status: "error", message: "Invalid content" });
      return;
    }

    const msg = {
      community_id: communityId,
      user_id: socket.user.id,
      content: content,
      type: type,
      created_at: new Date().toISOString(),
    };
    if (clientMessageId) msg.client_message_id = clientMessageId;

    // persist message using Supabase REST API (service key)
    try {
      const resp = await fetch(`${SUPABASE_URL}/rest/v1/messages`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${SUPABASE_SERVICE_KEY}`,
          apikey: SUPABASE_SERVICE_KEY,
          Prefer: "return=representation",
        },
        body: JSON.stringify(msg),
      });

      if (!resp.ok) {
        const text = await resp.text();
        console.error("Failed to save message", resp.status, text);

        // If the DB schema does not include client_message_id, the insert may fail with 400 PGRST204
        if (
          resp.status === 400 &&
          text &&
          text.includes("Could not find the 'client_message_id'")
        ) {
          try {
            console.warn("Retrying save without client_message_id");
            const msg2 = { ...msg };
            delete msg2.client_message_id;
            const resp2 = await fetch(`${SUPABASE_URL}/rest/v1/messages`, {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${SUPABASE_SERVICE_KEY}`,
                apikey: SUPABASE_SERVICE_KEY,
                Prefer: "return=representation",
              },
              body: JSON.stringify(msg2),
            });

            if (!resp2.ok) {
              const text2 = await resp2.text();
              console.error("Second save attempt failed", resp2.status, text2);
              if (callback)
                callback({
                  status: "error",
                  message: "Failed to save message",
                });
              return;
            }

            const saved2 = await resp2.json();
            const savedMsg2 =
              Array.isArray(saved2) && saved2.length > 0 ? saved2[0] : msg2;
            io.to(`community:${communityId}`).emit("message", {
              ...savedMsg2,
              user: { id: socket.user.id, email: socket.user.email },
            });

            if (callback) callback({ status: "ok", message: savedMsg2 });
            return;
          } catch (e2) {
            console.error("Retry without client_message_id failed", e2);
            if (callback)
              callback({ status: "error", message: "Failed to save message" });
            return;
          }
        }

        if (callback)
          callback({ status: "error", message: "Failed to save message" });
        return;
      }

      // read saved message (representation)
      const saved = await resp.json(); // array
      const savedMsg =
        Array.isArray(saved) && saved.length > 0 ? saved[0] : msg;

      // broadcast to community (include saved DB fields)
      io.to(`community:${communityId}`).emit("message", {
        ...savedMsg,
        user: { id: socket.user.id, email: socket.user.email },
      });

      if (callback) callback({ status: "ok", message: savedMsg });
    } catch (e) {
      console.error("send_message error", e);
      if (callback) callback({ status: "error", message: "Server error" });
    }
  });

  // Reaction event: accept either server message id or client_message_id, broadcast immediately
  socket.on("reaction", async (payload) => {
    try {
      const communityId = payload && payload.communityId;
      const messageId = payload && (payload.message_id || payload.messageId);
      const clientMessageId =
        payload && (payload.client_message_id || payload.clientMessageId);
      const liked = payload && payload.liked;
      if (!communityId || (!messageId && !clientMessageId)) return;

      const base = { liked: !!liked, user_id: socket.user.id };

      // Broadcast any server message id provided
      if (messageId) {
        io.to(`community:${communityId}`).emit("reaction", {
          ...base,
          message_id: messageId,
        });
      }

      // Broadcast client message id so clients with unsaved messages can apply reaction
      if (clientMessageId) {
        io.to(`community:${communityId}`).emit("reaction", {
          ...base,
          client_message_id: clientMessageId,
        });

        // Attempt to resolve client_message_id to saved message id and emit mapping if found
        try {
          const resp = await fetch(
            `${SUPABASE_URL}/rest/v1/messages?select=id&client_message_id=eq.${encodeURIComponent(
              clientMessageId
            )}`,
            {
              headers: {
                Authorization: `Bearer ${SUPABASE_SERVICE_KEY}`,
                apikey: SUPABASE_SERVICE_KEY,
              },
            }
          );
          if (resp.ok) {
            const rows = await resp.json();
            if (Array.isArray(rows) && rows.length > 0 && rows[0].id) {
              io.to(`community:${communityId}`).emit("reaction", {
                ...base,
                message_id: rows[0].id,
              });
            }
          }
        } catch (e) {
          console.warn(
            "Could not resolve client_message_id for reaction",
            clientMessageId,
            e
          );
        }
      }
    } catch (e) {
      console.error("reaction handler error", e);
    }
  });

  socket.on("disconnect", (reason) => {
    // handle disconnect if needed
  });
});

// On startup, perform a safe cleanup of legacy '<reaction>' and '1' messages: backup to local file and delete from DB
async function cleanupLegacyMessages() {
  try {
    const legacyQueries = [
      { desc: "<reaction>", filter: "content=eq.%3Creaction%3E" },
      { desc: "numeric '1'", filter: "content=eq.1" },
    ];

    const allBackups = [];

    for (const q of legacyQueries) {
      console.log("Checking legacy messages for:", q.desc);
      const res = await fetch(
        `${SUPABASE_URL}/rest/v1/messages?select=*&${q.filter}`,
        {
          headers: {
            Authorization: `Bearer ${SUPABASE_SERVICE_KEY}`,
            apikey: SUPABASE_SERVICE_KEY,
          },
        }
      );
      if (!res.ok) {
        console.warn(
          "Could not query legacy messages for",
          q.desc,
          "status",
          res.status
        );
        continue;
      }
      const rows = await res.json();
      if (rows && rows.length > 0) {
        console.log(`Found ${rows.length} legacy rows for ${q.desc}`);
        // Save to local backup file
        const fs = require("fs");
        const path = require("path");
        const bakDir = path.join(__dirname, "backups");
        if (!fs.existsSync(bakDir)) fs.mkdirSync(bakDir);
        const filename = path.join(
          bakDir,
          `messages_legacy_${q.desc.replace(
            /[^a-z0-9]/gi,
            "_"
          )}_${Date.now()}.json`
        );
        fs.writeFileSync(filename, JSON.stringify(rows, null, 2));
        console.log("Backed up rows to", filename);

        // Delete the legacy rows safely using DELETE with the same filter
        const del = await fetch(
          `${SUPABASE_URL}/rest/v1/messages?${q.filter}`,
          {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${SUPABASE_SERVICE_KEY}`,
              apikey: SUPABASE_SERVICE_KEY,
              Prefer: "return=minimal",
            },
          }
        );
        if (!del.ok) {
          const text = await del.text();
          console.error(
            "Failed to delete legacy rows for",
            q.desc,
            del.status,
            text
          );
        } else {
          console.log(`Deleted legacy rows for ${q.desc}`);
        }

        allBackups.push({ desc: q.desc, file: filename, count: rows.length });
      } else {
        console.log(`No legacy rows for ${q.desc}`);
      }
    }

    if (allBackups.length > 0)
      console.log("Legacy cleanup finished, backups created:", allBackups);
    else console.log("Legacy cleanup finished, no rows found.");
  } catch (e) {
    console.error("Error during legacy cleanup", e);
  }
}

server.listen(PORT, async () => {
  console.log(`Community server listening on ${PORT}`);
  // Run cleanup in background (do not block startup)
  cleanupLegacyMessages();
});
