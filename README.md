# Explore With Me — Stage 3: Comments Feature (Moderation)

**Pull Request:** [#6 — Stage 3: Implement additional feature (comments)](https://github.com/a-buianova/java-explore-with-me/pull/6)

This feature implements a full comment system for published events, including moderation workflow and reply hierarchy.

---

## 📘 Feature Overview

Users can leave comments on **published events**.  
Each new comment enters a **moderation queue** (`PENDING`) and becomes publicly visible only after admin approval (`PUBLISHED`).  
Admins may approve or reject comments; rejected ones remain hidden.

---

## ⚙️ Core Logic

| Action | Description |
|--------|--------------|
| **Create Comment** | Users post comments on published events. Initially `PENDING`. |
| **Moderation** | Admins can `approve` (→ `PUBLISHED`) or `reject` (→ `REJECTED`). |
| **Visibility** | Public API shows only `PUBLISHED` comments. |
| **Edit Window** | Authors can edit only `PUBLISHED` comments within **24 hours**. |
| **Replies** | Allowed only to `PUBLISHED` parent comments. |
| **Deletion** | User deletion = **hard delete**. No soft flag. |

---

## 🧩 Endpoints

### 🔹 Public API
```
GET /events/{eventId}/comments?from=0&size=10
GET /comments/{commentId}
```
→ Returns only **PUBLISHED** comments.

### 🔹 Private API (User)
```
POST   /users/{userId}/comments/events/{eventId}   // Create (PENDING)
PATCH  /users/{userId}/comments/{commentId}        // Edit own (if PUBLISHED & <24h)
DELETE /users/{userId}/comments/{commentId}        // Hard delete
GET    /users/{userId}/comments                    // List all own comments
```

### 🔹 Admin API
```
GET    /admin/comments                             // Pending moderation queue
PATCH  /admin/comments/{commentId}/approve         // Approve (→ PUBLISHED)
PATCH  /admin/comments/{commentId}/reject          // Reject  (→ REJECTED)
```

---

## 🧱 Data Model

`Comment` entity fields:
| Field | Type | Description |
|--------|------|-------------|
| `id` | Long | Unique identifier |
| `text` | String (10–2000) | Comment body |
| `author` | User | Author of comment |
| `event` | Event | Target event |
| `parentComment` | Comment | Optional reply parent |
| `state` | Enum (`PENDING`, `PUBLISHED`, `REJECTED`) |
| `creationDate` / `updateDate` | LocalDateTime | Auto timestamps |
| `edited` | boolean | True if user edited after creation |
| `version` | Long | Optimistic locking for moderation |

---

## 🧪 Testing

A dedicated Postman collection `feature.json` verifies:
- Comment creation (`PENDING`)  
- Moderation queue retrieval  
- Approve / Reject flow  
- Edit within 24h window  
- Replies only to `PUBLISHED` comments  
- Public visibility restricted to `PUBLISHED`

All functional and edge cases are covered.

---

## 🧾 Technical Details

- **Language:** Java 21  
- **Framework:** Spring Boot 3.3.x  
- **Database:** PostgreSQL  
- **Persistence:** Spring Data JPA  
- **Mapping:** MapStruct  
- **Validation:** Jakarta Validation  
- **Transaction management:** `@Transactional`  
- **Pagination utility:** custom `PageUtil.byFromSize()`  
- **Optimistic Locking:** via `@Version`

---

## ✅ Compliance with Technical Specification

- Matches Stage 3 requirements from project brief.  
- Implements **Comment entity**, moderation workflow, pagination, and reply hierarchy.  
- Validations and error handling conform to API specification.  
- Fully integrated into existing Explore With Me architecture.

---

**Author:** [Anastasia Buianova](https://github.com/a-buianova)  
**Feature:** Comments with moderation (`feature_comments` branch)  
**PR:** [#6 — Stage 3: Implement additional feature (comments)](https://github.com/a-buianova/java-explore-with-me/pull/6)
