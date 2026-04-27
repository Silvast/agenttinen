# AGENTS.md

## Project Overview

This is a Clojure CLI application that interfaces with the OpenCode AI API to generate chat completions. It is a simple, focused tool that sends prompts to `opencode.ai/zen/v1/chat/completions` and returns the AI's response.

## Stack

- **Language:** Clojure 1.12+
- **HTTP Client:** clj-http
- **JSON Parsing:** Cheshire
- **Build tool:** Leiningen
- **Testing:** clojure.test

## Project Structure

```
src/
  agenttinen/
    core.clj          ; Entry point, API client, main function
test/
  agenttinen/
    core_test.clj     ; Unit tests
```

## Commands

```bash
# Run the application
lein run

# Run with explicit main
lein run -m agenttinen.core

# Run tests
lein test

# Build uberjar
lein uberjar
```

## Development Workflow

- This is a CLI tool, not a long-running server — simple `lein run` or REPL evaluation is sufficient.
- For REPL development: `lein repl` and evaluate expressions directly.
- No hot-reload or system restart patterns needed (unlike web services).

## Coding Conventions

- **Namespaces:** `agenttinen.<module>` — e.g. `agenttinen.core`
- **Minimal structure:** Since this is a small CLI tool, all logic is in `core.clj`. For larger features, consider splitting into separate namespaces.
- **No global state:** Avoid `def` for mutable state; use `let` bindings and pure functions.
- **Error handling:** Use `ex-info` for structured exceptions or return error maps like `{:ok false :error "..."}`.
- **Prefer `->` threading** over deeply nested calls.
- **No `println` in production code.** Consider using a logging library if needed.
- **Environment variables:** Read via `System/getenv` — see Environment Variables section below.

## Testing Guidelines

- Unit test pure functions.
- The existing test uses `clojure.test`.
- Aim for meaningful test assertions (the current template test deliberately fails).

## Environment Variables

| Variable           | Description                              | Required |
|-------------------|------------------------------------------|----------|
| `OPENCODE_API_KEY` | API key for opencode.ai authentication | Yes      |

Example:
```bash
export OPENCODE_API_KEY="your-api-key-here"
lein run
```

## Pull Request Checklist

- [ ] `lein test` passes
- [ ] New functions have unit tests
- [ ] No `println` or commented-out code left in
- [ ] CHANGELOG updated if user-facing behavior changed

## Application Usage

```bash
# Set your API key
export OPENCODE_API_KEY="your-key"

# Run the app
lein run
```

The application will:
1. Read the `OPENCODE_API_KEY` environment variable
2. Send a system prompt (Finnish: "Olet avulias koodausassistentti, joka vastaa suomeksi") and user prompt to OpenCode AI
3. Print the response or error to stdout