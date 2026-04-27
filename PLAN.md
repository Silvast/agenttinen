# Plan: Add HTTP API Server for OpenCode Chat

## Context
This app currently sends a static system prompt and user prompt to OpenCode API via command-line execution. The goal is to transform it into a local HTTP server on port 3000 that:
- Loads system prompt from `system_prompt.md` file on filesystem
- Exposes an API endpoint that receives user prompt in request body
- Returns the OpenCode API response

## Approach
1. Use **Ring with Jetty adapter** for HTTP server (simple and reliable)
   - Note: Originally planned Reitit but switched to basic Ring due to dependency availability
2. Load system prompt from `resources/system_prompt.md` at runtime
3. Create `configuration.edn` for API URL and other settings
4. Add POST endpoint `/api/chat` that accepts JSON body with `user-prompt` field
5. Reuse existing `chat-completion` function with config from EDN

## Files to Modify

### New Files to Create
- `resources/system_prompt.md` - System prompt content
- `configuration/default.edn` - Configuration file with API settings

### Existing Files to Modify
- `project.clj` - Add Ring/Compojure dependencies
- `src/agenttinen/core.clj` - Add API server and configuration loading

## Reuse
- Existing `chat-completion` function in `src/agenttinen/core.clj`
- Existing `clj-http` and `cheshire` dependencies

## Steps
- [x] Step 1: Add Ring dependencies to project.clj
- [x] Step 2: Create resources/system_prompt.md with system prompt content
- [x] Step 3: Create configuration/default.edn with API URL
- [x] Step 4: Modify core.clj to:
  - [x] Load configuration from configuration/default.edn
  - [x] Load system prompt from resources/system_prompt.md
  - [x] Add Ring handler with POST /api/chat endpoint
  - [x] Add -main function to start server on port 3000
- [x] Step 5: Verify server starts and endpoint works

## Verification
1. Run `lein run` or `lein ring server` to start server on port 3000
2. Test with curl:
   ```bash
   curl -X POST http://localhost:3000/api/chat \
     -H "Content-Type: application/json" \
     -d '{"user-prompt": "Kerro Clojuren vahvuudet"}'
   ```
3. Verify response contains answer from OpenCode API