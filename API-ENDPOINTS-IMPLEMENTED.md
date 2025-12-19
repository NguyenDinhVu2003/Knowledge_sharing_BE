# 🔷 API ENDPOINTS CHO ANGULAR FRONTEND

**Base URL:** `http://localhost:8080/api`

**Last Updated:** December 19, 2025

---

## 🔐 AUTH SERVICE

### 1. Login
```
POST /api/auth/login
Headers: { "Content-Type": "application/json" }

Request Body:
{
  "username": "admin",
  "password": "admin123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```

### 2. Register
```
POST /api/auth/register
Headers: { "Content-Type": "application/json" }

Request Body:
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123"
}

Response 200:
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 2,
    "username": "newuser",
    "email": "newuser@example.com",
    "role": "EMPLOYEE"
}
```

### 3. Logout
```
POST /api/auth/logout
Headers: { "Authorization": "Bearer {token}" }

Response: 204 No Content
```

### 4. Get Current User
```
GET /api/auth/me
Headers: { "Authorization": "Bearer {token}" }

Response 200:
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

---

## 📄 DOCUMENT SERVICE

### 1. Upload Document
```
POST /api/documents
Headers: { "Authorization": "Bearer {token}" }
Content-Type: multipart/form-data

Request Body (FormData):
- data: {
    "title": "Angular Complete Guide",
    "summary": "Comprehensive Angular tutorial",
    "tags": ["Angular", "Frontend", "TypeScript"],
    "sharingLevel": "PUBLIC"
  }
- file: <binary file>

Response 200:
{
  "id": 1,
  "title": "Angular Complete Guide",
  "summary": "Comprehensive Angular tutorial",
  "tags": ["Angular", "Frontend", "TypeScript"],
  "sharingLevel": "PUBLIC",
  "fileType": "PDF",
  "fileUrl": "/uploads/abc123.pdf",
  "createdAt": "2025-12-20T10:00:00Z",
  "owner": {
    "id": 1,
    "username": "admin"
  }
}
```

### 2. Update Document
```
PUT /api/documents/{id}
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "title": "Updated Title",
  "summary": "Updated summary",
  "tags": ["Angular", "Tutorial"],
  "sharingLevel": "PRIVATE"
}

Response 200:
{
  "id": 1,
  "title": "Updated Title",
  "summary": "Updated summary",
  "tags": ["Angular", "Tutorial"],
  "sharingLevel": "PRIVATE",
  "updatedAt": "2025-12-20T11:00:00Z"
}
```

### 3. Get Document Detail
```
GET /api/documents/{id}
Headers: { "Authorization": "Bearer {token}" }

Response 200:
{
  "id": 1,
  "title": "Angular Complete Guide",
  "summary": "Comprehensive Angular tutorial",
  "tags": ["Angular", "Frontend"],
  "sharingLevel": "PUBLIC",
  "fileType": "PDF",
  "fileUrl": "/uploads/abc123.pdf",
  "owner": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com"
  },
  "createdAt": "2025-12-20T10:00:00Z",
  "ratings": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Excellent!",
      "user": { "username": "user1" }
    }
  ],
  "favorites": 10
}
```

### 4. Delete Document
```
DELETE /api/documents/{id}
Headers: { "Authorization": "Bearer {token}" }

Response: 204 No Content
```

### 5. Archive Document
```
DELETE /api/documents/{id}/archive
Headers: { "Authorization": "Bearer {token}" }

Response: 204 No Content
```

### 6. Get All Documents
```
GET /api/documents?sort=recent&limit=10&owner=me
Headers: { "Authorization": "Bearer {token}" }

Query Parameters:
  - sort: "recent" | "popular" (optional)
  - limit: number (optional, default: 10)
  - owner: "me" (optional, filter by current user)

Response 200:
[
  {
    "id": 1,
    "title": "Angular Guide",
    "summary": "Complete guide",
    "tags": ["Angular"],
    "sharingLevel": "PUBLIC",
    "fileType": "PDF",
    "fileUrl": "/uploads/abc123.pdf",
    "owner": {
      "id": 1,
      "username": "admin"
    },
    "createdAt": "2025-12-20T10:00:00Z"
  },
  {
    "id": 2,
    "title": "React Tutorial",
    "summary": "React basics",
    "tags": ["React"],
    "sharingLevel": "PUBLIC",
    "fileType": "DOC",
    "fileUrl": "/uploads/def456.doc",
    "owner": {
      "id": 2,
      "username": "user1"
    },
    "createdAt": "2025-12-19T15:30:00Z"
  }
]
```

### 7. Search Documents
```
GET /api/documents/search?query=angular&page=0&size=10
Headers: { "Authorization": "Bearer {token}" }

Query Parameters:
  - query: string (required)
  - tags: string[] (optional)
  - sharingLevel: string (optional)
  - page: number (optional, default: 0)
  - size: number (optional, default: 10)
  - sortBy: string (optional)

Response 200:
{
  "content": [
    {
      "id": 1,
      "title": "Angular Guide",
      "summary": "Complete guide",
      "tags": ["Angular", "Frontend"],
      "sharingLevel": "PUBLIC",
      "fileType": "PDF",
      "fileUrl": "/uploads/abc123.pdf",
      "owner": {
        "id": 1,
        "username": "admin"
      },
      "createdAt": "2025-12-20T10:00:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "number": 0,
  "size": 10
}
```

---

## 🔍 SEARCH SERVICE

### 1. Advanced Search
```
GET /api/search/advanced
Headers: { "Authorization": "Bearer {token}" }

Query Parameters:
  - query: string (optional)
  - tags: string[] (optional, e.g., tags=Angular&tags=React)
  - matchAllTags: boolean (optional)
  - sharingLevel: string (optional)
  - fileType: string (optional)
  - minRating: number (optional)
  - maxRating: number (optional)
  - fromDate: string (optional, ISO date)
  - toDate: string (optional, ISO date)
  - sortBy: "recent" | "popular" | "rating" (optional)
  - sortOrder: "asc" | "desc" (optional)
  - page: number (optional, default: 0)
  - size: number (optional, default: 10)
  - onlyFavorited: boolean (optional)

Example: /api/search/advanced?query=angular&tags=Frontend&minRating=4&sortBy=rating

Response 200:
{
  "content": [
    {
      "id": 1,
      "title": "Angular Advanced Techniques",
      "summary": "Deep dive into Angular",
      "tags": ["Angular", "Frontend"],
      "sharingLevel": "PUBLIC",
      "fileType": "PDF",
      "fileUrl": "/uploads/abc123.pdf",
      "owner": {
        "id": 1,
        "username": "admin"
      },
      "createdAt": "2025-12-20T10:00:00Z",
      "averageRating": 4.5,
      "totalRatings": 10
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "number": 0,
  "size": 10
}
```

### 2. Search with Facets
```
POST /api/search/with-facets
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "query": "angular",
  "filters": {
    "tags": ["Frontend", "TypeScript"],
    "sharingLevel": "PUBLIC",
    "fileType": "PDF"
  },
  "page": 0,
  "size": 10
}

Response 200:
{
  "documents": [
    {
      "id": 1,
      "title": "Angular Guide",
      "summary": "Complete guide",
      "tags": ["Angular", "Frontend"],
      "sharingLevel": "PUBLIC",
      "fileType": "PDF",
      "fileUrl": "/uploads/abc123.pdf",
      "owner": {
        "id": 1,
        "username": "admin"
      },
      "createdAt": "2025-12-20T10:00:00Z"
    }
  ],
  "facets": {
    "tags": {
      "Angular": 15,
      "Frontend": 12,
      "TypeScript": 8,
      "React": 5
    },
    "sharingLevels": {
      "PUBLIC": 20,
      "PRIVATE": 5,
      "GROUP": 3
    },
    "fileTypes": {
      "PDF": 18,
      "DOC": 7,
      "IMAGE": 3
    }
  }
}
```

---

## ⭐ FAVORITE SERVICE

### 1. Add to Favorites
```
POST /api/favorites/documents/{documentId}
Headers: { "Authorization": "Bearer {token}" }

Example: POST /api/favorites/documents/1

Response 200:
{
  "documentId": 1,
  "userId": 1,
  "createdAt": "2025-12-20T10:30:00Z"
}
```

### 2. Remove from Favorites
```
DELETE /api/favorites/documents/{documentId}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/favorites/documents/1

Response: 204 No Content
```

### 3. Get My Favorites
```
GET /api/favorites
Headers: { "Authorization": "Bearer {token}" }

Response 200:
[
  {
    "id": 1,
    "title": "Angular Guide",
    "summary": "Complete guide",
    "tags": ["Angular", "Frontend"],
    "sharingLevel": "PUBLIC",
    "fileType": "PDF",
    "fileUrl": "/uploads/abc123.pdf",
    "owner": {
      "id": 2,
      "username": "user1"
    },
    "createdAt": "2025-12-20T10:00:00Z"
  },
  {
    "id": 5,
    "title": "React Basics",
    "summary": "Introduction to React",
    "tags": ["React", "Frontend"],
    "sharingLevel": "PUBLIC",
    "fileType": "DOC",
    "fileUrl": "/uploads/def456.doc",
    "owner": {
      "id": 3,
      "username": "user2"
    },
    "createdAt": "2025-12-19T15:00:00Z"
  }
]
```

### 4. Check if Favorited
```
GET /api/favorites/documents/{documentId}/check
Headers: { "Authorization": "Bearer {token}" }

Example: GET /api/favorites/documents/1/check

Response 200:
{
  "isFavorited": true
}
```

### 5. Get Favorite Count
```
GET /api/favorites/documents/{documentId}/count

Example: GET /api/favorites/documents/1/count

Response 200:
{
  "count": 25
}
```

---

## 🔔 NOTIFICATION SERVICE

### 1. Get All Notifications
```
GET /api/notifications
Headers: { "Authorization": "Bearer {token}" }

Response 200:
[
  {
    "id": 1,
    "message": "New document shared: Angular Guide",
    "type": "DOCUMENT_SHARED",
    "read": false,
    "createdAt": "2025-12-20T10:00:00Z",
    "documentId": 5
  },
  {
    "id": 2,
    "message": "Your document received a rating",
    "type": "RATING_RECEIVED",
    "read": true,
    "createdAt": "2025-12-19T15:30:00Z",
    "documentId": 3
  }
]
```

### 2. Get Unread Notifications
```
GET /api/notifications/unread
Headers: { "Authorization": "Bearer {token}" }

Response 200:
[
  {
    "id": 1,
    "message": "New document shared: Angular Guide",
    "type": "DOCUMENT_SHARED",
    "read": false,
    "createdAt": "2025-12-20T10:00:00Z",
    "documentId": 5
  }
]
```

### 3. Get Unread Count
```
GET /api/notifications/unread/count
Headers: { "Authorization": "Bearer {token}" }

Response 200:
{
  "count": 3
}
```

### 4. Mark as Read
```
PUT /api/notifications/{id}/read
Headers: { "Authorization": "Bearer {token}" }

Example: PUT /api/notifications/1/read

Response: 204 No Content
```

### 5. Mark All as Read
```
PUT /api/notifications/read-all
Headers: { "Authorization": "Bearer {token}" }

Response: 204 No Content
```

### 6. Delete Notification
```
DELETE /api/notifications/{id}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/notifications/1

Response: 204 No Content
```

---

## 🏷️ TAG SERVICE

### 1. Create Tag
```
POST /api/tags
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "name": "Angular"
}

Response 200:
{
  "id": 1,
  "name": "Angular",
  "createdAt": "2025-12-20T10:00:00Z"
}
```

### 2. Update Tag
```
PUT /api/tags/{id}
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "name": "Angular Framework"
}

Response 200:
{
  "id": 1,
  "name": "Angular Framework",
  "updatedAt": "2025-12-20T11:00:00Z"
}
```

### 3. Delete Tag
```
DELETE /api/tags/{id}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/tags/1

Response: 204 No Content
```

### 4. Get All Tags
```
GET /api/tags
Headers: { "Authorization": "Bearer {token}" }

Response 200:
[
  {
    "id": 1,
    "name": "Angular"
  },
  {
    "id": 2,
    "name": "React"
  },
  {
    "id": 3,
    "name": "Vue"
  }
]
```

### 5. Get Popular Tags
```
GET /api/tags/popular?limit=10

Query Parameters:
  - limit: number (optional, default: 10)

Example: GET /api/tags/popular?limit=5

Response 200:
[
  {
    "name": "Angular",
    "count": 25
  },
  {
    "name": "React",
    "count": 18
  },
  {
    "name": "Vue",
    "count": 12
  }
]
```

### 6. Search Tags
```
GET /api/tags/search?keyword=angular

Query Parameters:
  - keyword: string (required)

Example: GET /api/tags/search?keyword=ang

Response 200:
[
  {
    "id": 1,
    "name": "Angular"
  },
  {
    "id": 5,
    "name": "AngularJS"
  }
]
```

---

## 💝 USER INTERESTS SERVICE

### 1. Get My Interests
```
GET /api/user-interests
Headers: { "Authorization": "Bearer {token}" }

Response 200:
{
  "interests": ["Angular", "React", "TypeScript", "Node.js"]
}
```

### 2. Update All Interests
```
PUT /api/user-interests
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "interests": ["Angular", "Vue", "Docker"]
}

Response 200:
{
  "interests": ["Angular", "Vue", "Docker"]
}
```

### 3. Add Interest
```
POST /api/user-interests/{tagName}
Headers: { "Authorization": "Bearer {token}" }

Example: POST /api/user-interests/Kubernetes

Response 200:
{
  "interests": ["Angular", "React", "TypeScript", "Node.js", "Kubernetes"]
}
```

### 4. Remove Interest
```
DELETE /api/user-interests/{tagName}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/user-interests/React

Response 200:
{
  "interests": ["Angular", "TypeScript", "Node.js", "Kubernetes"]
}
```

---

## ⭐ RATING SERVICE

### 1. Rate Document
```
POST /api/ratings/documents/{documentId}
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "rating": 5,
  "comment": "Excellent guide! Very comprehensive."
}

Response 200:
{
  "id": 1,
  "rating": 5,
  "comment": "Excellent guide! Very comprehensive.",
  "userId": 1,
  "documentId": 1,
  "createdAt": "2025-12-20T10:30:00Z"
}
```

### 2. Update Rating
```
PUT /api/ratings/documents/{documentId}
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "rating": 4,
  "comment": "Updated: Good but could be better"
}

Response 200:
{
  "id": 1,
  "rating": 4,
  "comment": "Updated: Good but could be better",
  "updatedAt": "2025-12-20T11:00:00Z"
}
```

### 3. Delete Rating
```
DELETE /api/ratings/documents/{documentId}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/ratings/documents/1

Response: 204 No Content
```

### 4. Get My Rating
```
GET /api/ratings/documents/{documentId}/my-rating
Headers: { "Authorization": "Bearer {token}" }

Example: GET /api/ratings/documents/1/my-rating

Response 200:
{
  "id": 1,
  "rating": 5,
  "comment": "Excellent guide!",
  "createdAt": "2025-12-20T10:30:00Z"
}

Response 404 (if not rated):
null
```

### 5. Get All Ratings
```
GET /api/ratings/documents/{documentId}

Example: GET /api/ratings/documents/1

Response 200:
[
  {
    "id": 1,
    "rating": 5,
    "comment": "Excellent guide!",
    "user": {
      "username": "user1"
    },
    "createdAt": "2025-12-20T10:30:00Z"
  },
  {
    "id": 2,
    "rating": 4,
    "comment": "Very helpful",
    "user": {
      "username": "admin"
    },
    "createdAt": "2025-12-19T15:00:00Z"
  }
]
```

### 6. Get Rating Stats
```
GET /api/ratings/documents/{documentId}/stats

Example: GET /api/ratings/documents/1/stats

Response 200:
{
  "averageRating": 4.5,
  "totalRatings": 10,
  "distribution": {
    "1": 0,
    "2": 1,
    "3": 2,
    "4": 3,
    "5": 4
  }
}
```

---

## 👑 ADMIN SERVICE

### 1. Get All Users
```
GET /api/admin/users
Headers: { "Authorization": "Bearer {token}" }

Response 200:
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN",
    "createdAt": "2025-01-15T10:00:00Z"
  },
  {
    "id": 2,
    "username": "user1",
    "email": "user1@example.com",
    "role": "EMPLOYEE",
    "createdAt": "2025-02-20T14:30:00Z"
  }
]
```

### 2. Get User Detail
```
GET /api/admin/users/{userId}
Headers: { "Authorization": "Bearer {token}" }

Example: GET /api/admin/users/1

Response 200:
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN",
  "createdAt": "2025-01-15T10:00:00Z",
  "documents": [
    {
      "id": 1,
      "title": "Angular Guide",
      "createdAt": "2025-12-20T10:00:00Z"
    }
  ],
  "ratings": [
    {
      "id": 1,
      "rating": 5,
      "documentId": 2
    }
  ]
}
```

### 3. Update User Role
```
PUT /api/admin/users/{userId}/role
Headers: { 
  "Authorization": "Bearer {token}",
  "Content-Type": "application/json"
}

Request Body:
{
  "role": "ADMIN"
}

Response 200:
{
  "id": 2,
  "username": "user1",
  "role": "ADMIN",
  "updatedAt": "2025-12-20T11:00:00Z"
}
```

### 4. Delete User
```
DELETE /api/admin/users/{userId}
Headers: { "Authorization": "Bearer {token}" }

Example: DELETE /api/admin/users/5

Response: 204 No Content
```

### 5. Get System Statistics
```
GET /api/admin/statistics
Headers: { "Authorization": "Bearer {token}" }

Response 200:
{
  "totalUsers": 150,
  "activeUsers": 120,
  "newUsersThisMonth": 25,
  "totalDocuments": 500,
  "documentsThisMonth": 45,
  "documentsByType": {
    "PDF": 300,
    "DOC": 150,
    "IMAGE": 50
  },
  "documentsBySharingLevel": {
    "PUBLIC": 350,
    "PRIVATE": 100,
    "GROUP": 50
  },
  "totalRatings": 1200,
  "averageRating": 4.2,
  "totalFavorites": 800,
  "totalNotifications": 3000,
  "totalTags": 50,
  "totalGroups": 15,
  "topTags": {
    "Angular": 120,
    "React": 95,
    "Vue": 60,
    "Spring Boot": 85,
    "Docker": 70
  },
  "topContributors": {
    "admin": 45,
    "user1": 38,
    "user2": 32,
    "user3": 28,
    "user4": 25
  },
  "topRatedDocuments": {
    "Angular Complete Guide": 4.9,
    "React Best Practices": 4.8,
    "Spring Boot Tutorial": 4.7,
    "Docker for Beginners": 4.6,
    "TypeScript Advanced": 4.5
  }
}
```

---

## 🔒 COMMON HEADERS

```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {JWT_TOKEN}"
}
```

---

## 🌐 ENVIRONMENT CONFIG

```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 📝 NOTES

1. **Authentication Required**: Most endpoints require `Authorization: Bearer {token}` header
2. **Pagination**: Default page size is 10, page index starts from 0
3. **File Upload**: Use `FormData` with `data` (JSON string) and `file` (File object)
4. **Date Format**: ISO 8601 format (e.g., `2025-12-19T10:30:00Z`)
5. **Error Response**: All errors return `{ status, message, timestamp }`

---

## 🚀 IMPLEMENTATION STATUS

- ✅ Auth Service (4 endpoints)
- ✅ Document Service (7 endpoints)
- ✅ Search Service (2 endpoints)
- ✅ Favorite Service (5 endpoints)
- ✅ Notification Service (6 endpoints)
- ✅ Tag Service (6 endpoints)
- ✅ User Interests Service (4 endpoints)
- ✅ Rating Service (6 endpoints)
- ✅ Admin Service (5 endpoints)

**Total: 45 API Endpoints**

