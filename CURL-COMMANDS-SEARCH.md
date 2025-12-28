# CURL Commands for Search API Endpoints

## ⚠️ IMPORTANT UPDATE - API REFACTORED

**All search functionality is now unified into ONE endpoint:**
```
GET /api/search
```

**Deprecated endpoints (removed):**
- ❌ `/api/search/advanced` 
- ❌ `/api/search/by-tags`
- ❌ `/api/search/favorites`

**Why?** All parameters are optional, so one endpoint handles both simple and advanced searches.
See `SEARCH-API-REFACTORING.md` for detailed explanation.

---

## Prerequisites
First, get authentication token:
```bash
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

echo "Token: $TOKEN"
```

---

## 1. GET /api/tags - Load filter options

### CURL Command:
```bash
curl -X GET "http://localhost:8090/api/tags" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

### Response (200 OK):
```json
[
  {
    "id": 1,
    "name": "Angular",
    "description": "Angular framework and related topics",
    "documentCount": 15,
    "createdAt": "2025-12-01T10:00:00"
  },
  {
    "id": 2,
    "name": "Spring Boot",
    "description": "Spring Boot backend development",
    "documentCount": 23,
    "createdAt": "2025-12-01T10:05:00"
  },
  {
    "id": 3,
    "name": "Java",
    "description": "Java programming language",
    "documentCount": 18,
    "createdAt": "2025-12-01T10:10:00"
  },
  {
    "id": 4,
    "name": "TypeScript",
    "description": "TypeScript language",
    "documentCount": 12,
    "createdAt": "2025-12-01T10:15:00"
  },
  {
    "id": 5,
    "name": "REST API",
    "description": "RESTful API design",
    "documentCount": 20,
    "createdAt": "2025-12-01T10:20:00"
  }
]
```

### Alternative - Get Popular Tags Only:
```bash
curl -X GET "http://localhost:8090/api/tags/popular?limit=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Alternative - Search Tags:
```bash
curl -X GET "http://localhost:8090/api/tags/search?keyword=angular" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 2. GET /api/groups - Load filter options

### CURL Command:
```bash
curl -X GET "http://localhost:8090/api/groups" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq
```

### Response (200 OK):
```json
[
  {
    "id": 1,
    "name": "Frontend Team",
    "description": "Frontend developers working on Angular projects",
    "memberCount": 8,
    "documentCount": 25,
    "memberUsernames": ["user1", "user2", "admin", "john_doe"],
    "createdAt": "2025-11-15T09:00:00",
    "updatedAt": "2025-12-15T14:30:00"
  },
  {
    "id": 2,
    "name": "Backend Team",
    "description": "Backend developers working on Spring Boot",
    "memberCount": 12,
    "documentCount": 35,
    "memberUsernames": ["admin", "user3", "user4", "jane_smith"],
    "createdAt": "2025-11-16T10:00:00",
    "updatedAt": "2025-12-14T11:20:00"
  },
  {
    "id": 3,
    "name": "DevOps Team",
    "description": "DevOps and infrastructure documentation",
    "memberCount": 5,
    "documentCount": 18,
    "memberUsernames": ["admin", "user5"],
    "createdAt": "2025-11-20T11:00:00",
    "updatedAt": "2025-12-10T16:45:00"
  }
]
```

### Alternative - Get My Groups Only:
```bash
curl -X GET "http://localhost:8090/api/groups/my-groups" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Alternative - Search Groups:
```bash
curl -X GET "http://localhost:8090/api/groups/search?keyword=frontend" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 3. GET /api/search - Unified Search (Simple & Advanced)

### Simple Keyword Search:
```bash
curl -X GET "http://localhost:8090/api/search?q=angular&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search with Sharing Level Filter:
```bash
curl -X GET "http://localhost:8090/api/search?sharingLevel=PRIVATE&page=0&size=10&sortBy=recent" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search with Tags Filter (OR logic):
```bash
curl -X GET "http://localhost:8090/api/search?tags=Angular,TypeScript&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search with Tags Filter (AND logic):
```bash
curl -X GET "http://localhost:8090/api/search?tags=Angular,TypeScript&matchAllTags=true&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search with File Type Filter:
```bash
curl -X GET "http://localhost:8090/api/search?fileType=PDF&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Combined Filters:
```bash
curl -X GET "http://localhost:8090/api/search?q=spring&sharingLevel=PUBLIC&fileType=PDF&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Response (200 OK):
```json
{
  "documents": [
    {
      "id": 1,
      "title": "Angular Best Practices Guide",
      "summary": "Comprehensive guide for Angular development best practices",
      "fileType": "PDF",
      "fileName": "angular-guide.pdf",
      "fileSize": 2048576,
      "sharingLevel": "PUBLIC",
      "tags": ["Angular", "TypeScript", "Frontend"],
      "versionNumber": 2,
      "ownerUsername": "john_doe",
      "ownerEmail": "john@example.com",
      "createdAt": "2025-12-01T10:30:00",
      "updatedAt": "2025-12-15T14:20:00",
      "isArchived": false,
      "averageRating": 4.5,
      "totalRatings": 12,
      "downloadCount": 45,
      "isFavorited": true
    }
  ],
  "currentPage": 0,
  "totalPages": 3,
  "totalElements": 25,
  "pageSize": 10,
  "tagFacets": null,
  "fileTypeFacets": null,
  "sharingLevelFacets": null,
  "ownerFacets": null,
  "query": "angular",
  "searchTimeMs": 45
}
```

---

## 4. Advanced Search Features (Same Endpoint)

### Search with Date Range:
```bash
curl -X GET "http://localhost:8090/api/search?q=java&fromDate=2025-12-01T00:00:00&toDate=2025-12-31T23:59:59&sortBy=recent&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search with Rating Filter:
```bash
curl -X GET "http://localhost:8090/api/search?minRating=4.0&maxRating=5.0&sortBy=rating&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search by Owner:
```bash
curl -X GET "http://localhost:8090/api/search?ownerUsername=admin&sortBy=recent&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search by Groups:
```bash
curl -X GET "http://localhost:8090/api/search?groupIds=1,2&sharingLevel=GROUP&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search Only Favorites:
```bash
curl -X GET "http://localhost:8090/api/search?onlyFavorited=true&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Search Favorites with Keyword:
```bash
curl -X GET "http://localhost:8090/api/search?onlyFavorited=true&q=angular&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Include Archived Documents:
```bash
curl -X GET "http://localhost:8090/api/search?includeArchived=true&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Complex Combined Search:
```bash
curl -X GET "http://localhost:8090/api/search?q=spring&tags=Java,Backend&matchAllTags=false&sharingLevel=PUBLIC&fileType=PDF&minRating=4.0&fromDate=2025-01-01T00:00:00&sortBy=rating&sortOrder=desc&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Response (200 OK):
```json
{
  "documents": [
    {
      "id": 3,
      "title": "Spring Boot Microservices",
      "summary": "Building scalable microservices with Spring Boot",
      "fileType": "PDF",
      "fileName": "spring-microservices.pdf",
      "fileSize": 3145728,
      "sharingLevel": "PUBLIC",
      "tags": ["Spring Boot", "Java", "Microservices", "Backend"],
      "versionNumber": 3,
      "ownerUsername": "admin",
      "ownerEmail": "admin@example.com",
      "createdAt": "2025-11-20T11:00:00",
      "updatedAt": "2025-12-18T16:30:00",
      "isArchived": false,
      "averageRating": 4.9,
      "totalRatings": 25,
      "downloadCount": 120,
      "isFavorited": true
    }
  ],
  "currentPage": 0,
  "totalPages": 2,
  "totalElements": 15,
  "pageSize": 10,
  "tagFacets": null,
  "fileTypeFacets": null,
  "sharingLevelFacets": null,
  "ownerFacets": null,
  "query": "spring",
  "searchTimeMs": 52
}
```


---

## 5. POST /api/search/facets - Search with facets for filtering UI

### CURL Command:
```bash
curl -X POST "http://localhost:8090/api/search/facets" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "angular",
    "page": 0,
    "size": 10,
    "sortBy": "recent"
  }' | jq
```

### Response (200 OK) - With Facets:
```json
{
  "documents": [
    {
      "id": 1,
      "title": "Angular Best Practices Guide",
      "summary": "Comprehensive guide for Angular development best practices",
      "fileType": "PDF",
      "fileName": "angular-guide.pdf",
      "fileSize": 2048576,
      "sharingLevel": "PUBLIC",
      "tags": ["Angular", "TypeScript", "Frontend"],
      "versionNumber": 2,
      "ownerUsername": "john_doe",
      "ownerEmail": "john@example.com",
      "createdAt": "2025-12-01T10:30:00",
      "updatedAt": "2025-12-15T14:20:00",
      "isArchived": false,
      "averageRating": 4.5,
      "totalRatings": 12,
      "downloadCount": 45,
      "isFavorited": true
    }
  ],
  "currentPage": 0,
  "totalPages": 3,
  "totalElements": 25,
  "pageSize": 10,
  "tagFacets": {
    "Angular": 25,
    "TypeScript": 18,
    "Frontend": 22,
    "Components": 15,
    "RxJS": 12,
    "NgRx": 8
  },
  "fileTypeFacets": {
    "PDF": 18,
    "DOCX": 5,
    "PPTX": 2
  },
  "sharingLevelFacets": {
    "PUBLIC": 20,
    "GROUP": 4,
    "PRIVATE": 1
  },
  "ownerFacets": {
    "john_doe": 8,
    "jane_smith": 6,
    "admin": 5,
    "user1": 3,
    "user2": 3
  },
  "query": "angular",
  "searchTimeMs": 68
}
```

### Advanced Faceted Search with Filters:
```bash
curl -X POST "http://localhost:8090/api/search/facets" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "spring",
    "tags": ["Java", "Backend"],
    "matchAllTags": false,
    "fileType": "PDF",
    "minRating": 4.0,
    "sortBy": "rating",
    "page": 0,
    "size": 10
  }' | jq
```

---

## 6. All Available Parameters (GET /api/search)

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | string | Keyword search | `?q=angular` |
| `tags` | string[] | Filter by tags (comma-separated) | `?tags=Java,Spring` |
| `matchAllTags` | boolean | AND logic for tags (default: false) | `?matchAllTags=true` |
| `sharingLevel` | enum | PUBLIC, PRIVATE, GROUP | `?sharingLevel=PUBLIC` |
| `fileType` | enum | PDF, DOCX, XLSX, etc | `?fileType=PDF` |
| `ownerId` | number | Filter by owner ID | `?ownerId=5` |
| `ownerUsername` | string | Filter by owner username | `?ownerUsername=admin` |
| `groupIds` | number[] | Filter by groups (comma-separated) | `?groupIds=1,2,3` |
| `minRating` | number | Minimum rating (0-5) | `?minRating=4.0` |
| `maxRating` | number | Maximum rating (0-5) | `?maxRating=5.0` |
| `fromDate` | datetime | Created after (ISO-8601) | `?fromDate=2025-01-01T00:00:00` |
| `toDate` | datetime | Created before (ISO-8601) | `?toDate=2025-12-31T23:59:59` |
| `sortBy` | enum | recent, oldest, title, rating, popular, relevance | `?sortBy=rating` |
| `sortOrder` | enum | asc, desc | `?sortOrder=desc` |
| `page` | number | Page number (default: 0) | `?page=0` |
| `size` | number | Page size (default: 10, max: 100) | `?size=20` |
| `includeArchived` | boolean | Include archived (default: false) | `?includeArchived=true` |
| `onlyFavorited` | boolean | Only favorites (default: false) | `?onlyFavorited=true` |

---

## 7. Common Use Cases

### Use Case 1: Simple header search bar
```bash
curl "http://localhost:8090/api/search?q=angular" -H "Authorization: Bearer $TOKEN"
```

### Use Case 2: Filter sidebar - by sharing level
```bash
curl "http://localhost:8090/api/search?sharingLevel=PUBLIC" -H "Authorization: Bearer $TOKEN"
```

### Use Case 3: Filter sidebar - by file type
```bash
curl "http://localhost:8090/api/search?fileType=PDF" -H "Authorization: Bearer $TOKEN"
```

### Use Case 4: Tag cloud navigation
```bash
curl "http://localhost:8090/api/search?tags=Java,Spring&matchAllTags=false" -H "Authorization: Bearer $TOKEN"
```

### Use Case 5: My favorites page
```bash
curl "http://localhost:8090/api/search?onlyFavorited=true" -H "Authorization: Bearer $TOKEN"
```

### Use Case 6: Advanced search form
```bash
curl "http://localhost:8090/api/search?q=spring&tags=Java,Backend&minRating=4.0&fromDate=2025-01-01T00:00:00&toDate=2025-12-31T23:59:59&sortBy=rating&sortOrder=desc" -H "Authorization: Bearer $TOKEN"
```

---

## 8. Error Responses

### 401 Unauthorized (Missing or invalid token):
```json
{
  "status": 401,
  "message": "Unauthorized - Invalid or missing token",
  "timestamp": "2025-12-22T10:30:00"
}
```

### 400 Bad Request (Invalid parameters):
```json
{
  "status": 400,
  "message": "Invalid request parameters",
  "timestamp": "2025-12-22T10:30:00"
}
```

### 500 Internal Server Error:
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2025-12-22T10:30:00"
}
```

---

## 9. Testing Script (PowerShell)

Save this as `test-search-apis.ps1`:

```powershell
# Get token
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8090/api/auth/login" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'

$token = $loginResponse.token
Write-Host "Token: $token`n"

# Test 1: Get Tags
Write-Host "=== Test 1: Get Tags ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/tags" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 2: Get Groups
Write-Host "`n=== Test 2: Get Groups ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/groups" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 3: Simple Search
Write-Host "`n=== Test 3: Simple Search ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?q=angular&page=0&size=10" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 4: Search by Sharing Level
Write-Host "`n=== Test 4: Search by Sharing Level ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?sharingLevel=PUBLIC" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 5: Search with Rating Filter
Write-Host "`n=== Test 5: Search with Rating Filter ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?minRating=4.0&sortBy=rating" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 6: Search Tags with AND logic
Write-Host "`n=== Test 6: Search Tags (AND) ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?tags=Angular,TypeScript&matchAllTags=true" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 7: Search Favorites
Write-Host "`n=== Test 7: Search Favorites ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?onlyFavorited=true" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 8: Complex Search
Write-Host "`n=== Test 8: Complex Combined Search ==="
Invoke-RestMethod -Uri "http://localhost:8090/api/search?q=spring&tags=Java,Backend&fileType=PDF&minRating=4.0&sortBy=rating" `
  -Headers @{Authorization="Bearer $token"} | ConvertTo-Json -Depth 10

# Test 9: Search with Facets
Write-Host "`n=== Test 9: Search with Facets ==="
$body = @{
  query = "angular"
  page = 0
  size = 10
  sortBy = "recent"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8090/api/search/facets" `
  -Method POST `
  -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} `
  -Body $body | ConvertTo-Json -Depth 10
```

Run with: `.\test-search-apis.ps1`

---

## 10. Quick Reference Table

| Use Case | Endpoint | Example |
|----------|----------|---------|
| Simple search | `GET /api/search` | `?q=angular` |
| Filter by level | `GET /api/search` | `?sharingLevel=PUBLIC` |
| Filter by type | `GET /api/search` | `?fileType=PDF` |
| Filter by tags (OR) | `GET /api/search` | `?tags=Java,Spring` |
| Filter by tags (AND) | `GET /api/search` | `?tags=Java,Spring&matchAllTags=true` |
| Filter by rating | `GET /api/search` | `?minRating=4.0&maxRating=5.0` |
| Filter by date | `GET /api/search` | `?fromDate=...&toDate=...` |
| Filter by owner | `GET /api/search` | `?ownerUsername=admin` |
| Filter by groups | `GET /api/search` | `?groupIds=1,2,3` |
| Only favorites | `GET /api/search` | `?onlyFavorited=true` |
| Include archived | `GET /api/search` | `?includeArchived=true` |
| With facets | `POST /api/search/facets` | Request body |
| Load tags | `GET /api/tags` | - |
| Load groups | `GET /api/groups` | - |

---

## Summary

✅ **ONE unified endpoint**: `/api/search`  
✅ **18 optional parameters**: Use only what you need  
✅ **Works for both simple & advanced**: No need for separate endpoints  
✅ **RESTful design**: Query params for filtering  
✅ **Frontend-friendly**: One service method handles all cases  

See `SEARCH-API-REFACTORING.md` for detailed explanation of why this design is better.

