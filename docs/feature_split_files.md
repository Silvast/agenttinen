# Plan: Split agenttinen/core.clj into opencode.clj, server.clj, and core.clj

## Context

The current `src/agenttinen/core.clj` is a single file (~151 lines) that contains:
1. Config loading (default.edn, system-prompt.md)
2. OpenAI/OpenCode API client code (chat-completion, HTTP client config)
3. HTTP handlers (chat-handler, root-handler)
4. Ring router and app definition
5. Server startup (jetty, -main)

The goal is to split into three focused modules:
- **opencode.clj** — API client code only
- **server.clj** — HTTP handlers and Ring setup
- **core.clj** — Config loading + app startup entry point

## Approach

Extract code by responsibility into separate namespaces:
- `opencode` handles all OpenAI/OpenCode API communication
- `server` handles HTTP endpoints and Jetty server
- `core` handles config + orchestrates startup

## Files to create

### 1. `src/agenttinen/opencode.clj` (new file)

Contains:
- `parse-json` — utility
- `to-json` — utility
- `client-config` — HTTP client config
- `chat-completion` — makes API calls

**Requires:** clj-http, cheshire, config (from core)

### 2. `src/agenttinen/server.clj` (new file)

Contains:
- `chat-handler` — POST /api/chat endpoint
- `root-handler` — GET / endpoint
- `app` — ring router + default handler
- `start-server` — starts Jetty

**Requires:** reitit.ring, ring.adapter.jetty, environ, opencode (for chat-completion)

### 3. `src/agenttinen/core.clj` (modify)

Keeps lines 91 onwards (from original file):
- `config` — loaded from default.edn
- `system-prompt` — loaded from resources/system_prompt.md
- `load-config` — helper function
- `default-config-file` — constant
- `app` definition (re-exported or called from server)
- `start-server` (calls server/start-server)
- `-main` — entry point

**Requires:** agenttinen.opencode (for client), agenttinen.server (for app/start-server)

## Reuse

Existing functions to move (not recreate):
- All functions currently in core.clj will be moved to their appropriate new modules
- No new logic needed — just reorganization

## Steps

- [ ] 1. Create `src/agenttinen/opencode.clj` with API client code
- [ ] 2. Create `src/agenttinen/server.clj` with HTTP handlers and app
- [ ] 3. Modify `src/agenttinen/core.clj` to only include config + startup (lines 91+)
- [ ] 4. Ensure `(ns ...)` declarations are correct in all three files
- [ ] 5. Update require statements to reference new namespaces

## Line Mapping from Original

| Original Lines | Content | New File |
|----------------|---------|----------|
| 1-8 | ns + requires | opencode.clj (partial) |
| 10-15 | default-config-file, load-config | core.clj |
| 17-22 | config, system-prompt, load-config fn | core.clj |
| 24-27 | client-config | opencode.clj |
| 29-48 | chat-completion fn | opencode.clj |
| 50-55 | parse-json, to-json fns | opencode.clj |
| 57-84 | chat-handler fn | server.clj |
| 86-90 | root-handler fn | server.clj |
| 92-98 | app (ring router) | server.clj |
| 100-103 | start-server fn | server.clj |
| 105-110 | -main fn | core.clj |

## Verification

- [ ] Run `clojure -M -m agenttinen.core` — app should start
- [ ] Test `GET /` returns "Agenttinen API running..."
- [ ] Test `POST /api/chat` with `{"user-prompt": "hello"}` returns completion
- [ ] All three namespaces load without errors