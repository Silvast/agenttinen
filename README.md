# agenttinen

FIXME: description

## Installation

## Usage

Start the server:

    lein run

The server listens on port **3000** by default (configurable via the `PORT` environment variable or `:server :port` in config).

## API

### POST `/api/chat`

Sends a chat message to the OpenCode API and returns the assistant's response.

**Request Parameters** (JSON body):

| Parameter    | Type   | Required | Description                                        |
|--------------|--------|----------|----------------------------------------------------|
| `user-prompt`| string | Yes      | The user's message / prompt to send to the model.  |
| `model`      | string | No       | The configured allowed model to use. Defaults to the server config model. |

**Response:**

| Status | Body                                    |
|--------|-----------------------------------------|
| 200    | `{"content": "<assistant response>"}`   |
| 400    | `{"error": "Missing user-prompt..."}` or `{"error": "Unsupported model"}` |
| 401    | `{"error": "OPENCODE_API_KEY not set"}` |
| 500    | `{"error": "<error details>"}`          |

**Example:**

```bash
curl -X POST http://localhost:3000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"model": "minimax-m2.5-free", "user-prompt": "Heippa!"}'
```

Request-selected models must be present in `:opencode :allowed-models`; when `model` is omitted, the server uses `:opencode :model`.

## License

Copyright © 2026 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
<https://www.eclipse.org/legal/epl-2.0>.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at <https://www.gnu.org/software/classpath/license.html>.
