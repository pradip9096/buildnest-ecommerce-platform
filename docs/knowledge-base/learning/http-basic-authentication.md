# HTTP Basic Authentication

**Category:** Backend > Security > Authentication  
**Tags:** `authentication`, `basic-auth`, `http`, `api-security`, `credentials`  
**Last Updated:** 2026-06-24

---

## What Is It?

Basic Authentication is the **simplest HTTP authentication mechanism** defined in [RFC 7617](https://datatracker.ietf.org/doc/html/rfc7617).

The client sends a **username and password on every request**, encoded in Base64, inside the `Authorization` HTTP header.

> **Analogy:** Like showing your ID card at every door you enter — not a keycard, not a badge — just your raw ID, every single time.

---

## Foundational Concept: Base64 Encoding ≠ Encryption

Before understanding Basic Auth, you must understand this:

| | Base64 Encoding | Encryption |
|---|---|---|
| **Purpose** | Text-safe transport format | Confidentiality |
| **Reversible?** | ✅ Yes — trivially | Only with the right key |
| **Protects data?** | ❌ No | ✅ Yes |

```
"pradip:secret123"
        ↓  base64 encode
"cHJhZGlwOnNlY3JldDEyMw=="
        ↓  base64 decode (by anyone)
"pradip:secret123"
```

> ⚠️ Base64 is just a **display format**, not security. This is why Basic Auth **requires HTTPS** — TLS is what actually encrypts the credentials in transit.

---

## How It Works

### Step-by-Step Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant S as API Server

    C->>S: GET /api/data (no credentials)
    S-->>C: 401 Unauthorized + WWW-Authenticate: Basic realm="API"

    note over C: Combines and encodes credentials
    note over C: base64("username:password")

    C->>S: GET /api/data + Authorization: Basic cHJhZGlwOnNlY3JldA==
    S->>S: Decode Base64
    S->>S: Verify credentials against DB
    S-->>C: 200 OK (or 401 if invalid)
```

### The Authorization Header

```http
Authorization: Basic cHJhZGlwOnNlY3JldA==
```

Decoded:
```
pradip:secret
```

Format is always: `base64(username + ":" + password)`

---

## Strengths & Weaknesses

| ✅ Use It For | ❌ Avoid It For |
|---|---|
| Internal admin tools (over HTTPS) | Public-facing REST APIs |
| Quick prototypes / local dev | Mobile or web applications |
| Server-to-server on private networks | Anything storing sensitive user data |
| CI/CD basic access control | Systems needing role-based access |

### Core Limitations

1. **Credentials sent every request** — more exposure, more risk
2. **No expiry** — compromised credentials stay valid until password changes
3. **No revocation** — you cannot invalidate a specific session
4. **No claims or roles** — server must look up permissions separately on every request

---

## Practical Example

### Server-Side Middleware (Node.js / Express)

```js
function basicAuth(req, res, next) {
  const authHeader = req.headers['authorization'];

  if (!authHeader || !authHeader.startsWith('Basic ')) {
    res.setHeader('WWW-Authenticate', 'Basic realm="API"');
    return res.status(401).json({ error: 'Authentication required' });
  }

  // Decode Base64
  const base64 = authHeader.split(' ')[1];
  const [username, password] = Buffer.from(base64, 'base64').toString().split(':');

  // Verify credentials (use hashed comparison in production)
  if (username === 'admin' && password === 'secret') {
    return next();
  }

  return res.status(401).json({ error: 'Invalid credentials' });
}
```

> ⚠️ Always compare password hashes (e.g., bcrypt), never plain strings in production.

---

## Basic Auth vs JWT — When to Upgrade

```mermaid
flowchart TD
    A["Do you need authentication?"] --> B{"Is it internal-only\nor a quick prototype?"}
    B -->|Yes| C{"Is HTTPS enforced?"}
    C -->|Yes| D["✅ Basic Auth is acceptable"]
    C -->|No| E["❌ Not safe — add HTTPS first"]
    B -->|No| F{"Public API, mobile app,\nor role-based access?"}
    F -->|Yes| G["✅ Use JWT instead"]
    F -->|No| H["Evaluate your specific needs"]
```

| Dimension | Basic Auth | JWT |
|---|---|---|
| Credentials sent | Every request | Once at login |
| Expiry | ❌ None | ✅ Configurable |
| Revocable | ❌ No | ⚠️ With short TTL + blacklist |
| Carries roles/claims | ❌ No | ✅ Yes |
| Implementation effort | Very low | Low–Medium |
| Suitable for production APIs | ❌ Rarely | ✅ Yes |

---

## Key Takeaways

1. **What it is:** Username + password, Base64-encoded, sent in the `Authorization` header on every request
2. **Base64 ≠ security** — it is reversible by anyone; HTTPS provides the actual encryption
3. **No expiry, no revocation, no roles** — it is intentionally primitive
4. **Acceptable for:** Internal tooling, private networks, quick prototypes — always over HTTPS
5. **Upgrade to JWT** when building public APIs, user-facing apps, or anything needing role-based access

---

## Related Articles

- API Authentication & RBAC Authorization
- JWT (JSON Web Token) explained
- OAuth 2.0 flow explained with examples
- API security best practices checklist
