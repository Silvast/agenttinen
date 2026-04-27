# AGENTS.md

## Project Overview

This is a Clojure backend service built with [e.g. Ring + Reitit ].
It follows a functional, data-oriented architecture. Prefer pure functions and immutable data structures throughout.

## Stack

- **Language:** Clojure 1.12+
- **HTTP:** Ring + Reitit
- **Dependency injection:** Integrant (or Component)
- **Database:** PostgreSQL via next.jdbc, see <https://github.com/seancorfield/next-jdbc/blob/develop/doc/friendly-sql-functions.md>
- **Testing:** clojure.test + kaocha
- **Build tool:** leiningen
- **Linting:** clj-kondo
- **Formatting:** cljfmt

## Project Structure

```
src/
  agenttinen/
    core.clj          ; Entry point / system init
    config.clj        ; Config loading (from env / aero)
    db/
      core.clj        ; DB connection pool
      queries.clj     ; SQL queries via HoneySQL
    http/
      router.clj      ; Route definitions
      middleware.clj  ; Ring middleware stack
    domain/
      user.clj        ; Business logic, pure functions
      order.clj
    api/
      user.clj        ; HTTP handlers (thin layer)
      order.clj
test/
  agenttinen/
    domain/
      user_test.clj
    api/
      user_test.clj
```

## Commands

```bash
# Start the REPL
lein repl 

# Run all tests
lein test

# Lint
lein lint          # runs clj-kondo

# Format check
lein fmt           # runs cljfmt check

# Format fix
lein fmt-fix       # runs cljfmt fix

# Build uberjar
lein build uber
```

## Development Workflow

- Start the system via `(dev/go)` in the REPL — never restart the whole process.
- Use `(dev/reset)` to reload changed namespaces and restart the system.
- The dev namespace is at `dev/user.clj` and is not included in production builds.

## Coding Conventions

- **Namespaces:** `myapp.<layer>.<noun>` — e.g. `myapp.domain.user`, `myapp.api.order`
- **No global state.** Pass the system map / db connection explicitly; do not use `def` for mutable state.
- **Handlers are thin.** HTTP handlers should only parse input, call a domain function, and return a response map. Business logic lives in `domain/`.
- **Use `ex-info` for errors.** Throw structured exceptions with `:type` key; catch at the middleware level.
- **Prefer `->` threading** over deeply nested calls.
- **Specs / Malli:** All public domain function inputs/outputs should have Malli schemas in a `schema.clj` or co-located.
- **No `println` in production code.** Use the logger (`taoensso.timbre` or `mulog`).

## Testing Guidelines

- Unit test pure domain functions exhaustively.
- Integration tests (hitting a real test DB) live under `test/integration/` and require `INTEGRATION=true` env var to run.
- Use `with-redefs` sparingly — prefer designing for testability via dependency injection.
- Aim for every public function in `domain/` to have at least one test.

## Environment Variables

Loaded via Aero from `resources/config.edn`. Required vars:

| Variable         | Description                  |
|-----------------|------------------------------|
| `DATABASE_URL`  | JDBC connection string       |
| `PORT`          | HTTP server port (default 3000) |
| `LOG_LEVEL`     | `debug` / `info` / `warn`    |

Never hardcode secrets. Never commit `.env` files.

## Pull Request Checklist

- [ ] `clj -M:test` passes
- [ ] `clj -M:lint` reports no errors
- [ ] `clj -M:fmt` reports no formatting issues
- [ ] New domain logic has unit tests
- [ ] No `println` or commented-out code left in
- [ ] CHANGELOG updated if user-facing behaviour changed

```

---

A few things worth noting about this template:

**Adapt the stack section** — swap in whatever you actually use (Compojure vs Reitit, Component vs Integrant, Timbre vs Mulog, etc.). Agents work best when the stack is stated precisely rather than left as examples.

**The REPL workflow section is Clojure-specific** and quite valuable — it tells the agent not to suggest `ctrl-c / restart` patterns, which would be wrong in a typical Clojure dev setup.

**Keep commands runnable as-is.** Agents will literally execute what's in the Commands section, so make sure the aliases match your actual `deps.edn`.

**The thin-handlers / domain-logic separation** in coding conventions helps the agent make correct architectural decisions without you having to re-explain it every time.
