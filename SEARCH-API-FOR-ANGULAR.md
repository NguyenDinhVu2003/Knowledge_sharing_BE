# Search API for Angular Integration

## 🎯 Unified Search Endpoint

**Endpoint:** `GET /api/search`  
**Auth:** Required (Bearer token)  
**Base URL:** `http://localhost:8090`

---

## Request Parameters (All Optional)

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | string | Keyword search | `angular` |
| `tags` | string | Comma-separated tags | `Java,Spring,Backend` |
| `matchAllTags` | boolean | AND logic (default: false) | `true` |
| `sharingLevel` | string | PUBLIC/PRIVATE/GROUP | `PUBLIC` |
| `fileType` | string | PDF/DOCX/XLSX/PPTX/TXT/IMAGE | `PDF` |
| `ownerId` | number | Filter by owner ID | `5` |
| `ownerUsername` | string | Filter by owner username | `admin` |
| `groupIds` | string | Comma-separated group IDs | `1,2,3` |
| `minRating` | number | Min rating 0-5 | `4.0` |
| `maxRating` | number | Max rating 0-5 | `5.0` |
| `fromDate` | string | ISO-8601 date | `2025-01-01T00:00:00` |
| `toDate` | string | ISO-8601 date | `2025-12-31T23:59:59` |
| `sortBy` | string | recent/oldest/title/rating/popular/relevance | `rating` |
| `sortOrder` | string | asc/desc | `desc` |
| `page` | number | Page number (default: 0) | `0` |
| `size` | number | Page size (default: 10) | `20` |
| `includeArchived` | boolean | Include archived (default: false) | `true` |
| `onlyFavorited` | boolean | Only favorites (default: false) | `true` |

---

## Response JSON Structure

```json
{
  "documents": [
    {
      "id": 1,
      "title": "Angular Best Practices Guide",
      "summary": "Comprehensive guide for Angular development",
      "content": null,
      "filePath": "ff9d263b-ec0f-4b2f-a6d6-7f0cc071f51a.png",
      "fileType": "IMAGE",
      "fileSize": 112993,
      "sharingLevel": "PUBLIC",
      "versionNumber": 1,
      "isArchived": false,
      "ownerId": 1,
      "ownerUsername": "admin",
      "averageRating": 5.0,
      "ratingCount": 1,
      "createdAt": "2025-12-17T01:07:23.288084",
      "updatedAt": "2025-12-17T01:07:23.288084",
      "tags": ["Angular", "Frontend"],
      "groupIds": []
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 5,
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

## Angular TypeScript Interface

```typescript
// Search filters interface
export interface SearchFilters {
  q?: string;
  tags?: string[];
  matchAllTags?: boolean;
  sharingLevel?: 'PUBLIC' | 'PRIVATE' | 'GROUP';
  fileType?: 'PDF' | 'DOCX' | 'XLSX' | 'PPTX' | 'TXT' | 'IMAGE';
  ownerId?: number;
  ownerUsername?: string;
  groupIds?: number[];
  minRating?: number;
  maxRating?: number;
  fromDate?: string;
  toDate?: string;
  sortBy?: 'recent' | 'oldest' | 'title' | 'rating' | 'popular' | 'relevance';
  sortOrder?: 'asc' | 'desc';
  page?: number;
  size?: number;
  includeArchived?: boolean;
  onlyFavorited?: boolean;
}

// Document interface
export interface Document {
  id: number;
  title: string;
  summary: string;
  content: string | null;
  filePath: string;
  fileType: string;
  fileSize: number;
  sharingLevel: string;
  versionNumber: number;
  isArchived: boolean;
  ownerId: number;
  ownerUsername: string;
  averageRating: number;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
  tags: string[];
  groupIds: number[];
}

// Search result interface
export interface SearchResultResponse {
  documents: Document[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  tagFacets: { [key: string]: number } | null;
  fileTypeFacets: { [key: string]: number } | null;
  sharingLevelFacets: { [key: string]: number } | null;
  ownerFacets: { [key: string]: number } | null;
  query: string | null;
  searchTimeMs: number;
}
```

---

## Angular Service Example

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private apiUrl = `${environment.apiBaseUrl}/api/search`;

  constructor(private http: HttpClient) {}

  /**
   * Unified search method - handles ALL search scenarios
   */
  search(filters: SearchFilters = {}): Observable<SearchResultResponse> {
    let params = new HttpParams();

    // Add all defined parameters
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        if (Array.isArray(value)) {
          // Convert array to comma-separated string
          params = params.set(key, value.join(','));
        } else {
          params = params.set(key, value.toString());
        }
      }
    });

    return this.http.get<SearchResultResponse>(this.apiUrl, { params });
  }
}
```

---

## Usage Examples in Angular Components

### 1. Simple keyword search
```typescript
this.searchService.search({ q: 'angular' }).subscribe(result => {
  this.documents = result.documents;
  this.totalPages = result.totalPages;
});
```

### 2. Filter by sharing level
```typescript
this.searchService.search({ sharingLevel: 'PUBLIC' }).subscribe(result => {
  this.documents = result.documents;
});
```

### 3. Filter by tags (OR logic)
```typescript
this.searchService.search({ 
  tags: ['Java', 'Spring', 'Backend'] 
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 4. Filter by tags (AND logic - must have all tags)
```typescript
this.searchService.search({ 
  tags: ['Angular', 'TypeScript'],
  matchAllTags: true 
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 5. Search with rating filter
```typescript
this.searchService.search({ 
  minRating: 4.0,
  sortBy: 'rating',
  sortOrder: 'desc'
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 6. Search with date range
```typescript
this.searchService.search({ 
  fromDate: '2025-01-01T00:00:00',
  toDate: '2025-12-31T23:59:59',
  sortBy: 'recent'
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 7. Search by owner
```typescript
this.searchService.search({ 
  ownerUsername: 'admin' 
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 8. Search only favorites
```typescript
this.searchService.search({ 
  onlyFavorited: true 
}).subscribe(result => {
  this.documents = result.documents;
});
```

### 9. Complex combined search
```typescript
this.searchService.search({ 
  q: 'spring',
  tags: ['Java', 'Backend'],
  sharingLevel: 'PUBLIC',
  fileType: 'PDF',
  minRating: 4.0,
  sortBy: 'rating',
  page: 0,
  size: 20
}).subscribe(result => {
  this.documents = result.documents;
  this.totalPages = result.totalPages;
  this.totalElements = result.totalElements;
});
```

### 10. Search with pagination
```typescript
loadPage(page: number) {
  this.searchService.search({ 
    ...this.currentFilters,
    page: page,
    size: 10
  }).subscribe(result => {
    this.documents = result.documents;
    this.currentPage = result.currentPage;
    this.totalPages = result.totalPages;
  });
}
```

---

## Additional Endpoints

### Get all tags (for filter dropdown)
**GET** `/api/tags`

Response:
```json
[
  {
    "id": 1,
    "name": "Angular",
    "description": "Angular framework",
    "documentCount": 15,
    "createdAt": "2025-12-01T10:00:00"
  }
]
```

### Get all groups (for filter dropdown)
**GET** `/api/groups`

Response:
```json
[
  {
    "id": 1,
    "name": "Frontend Team",
    "description": "Frontend developers",
    "memberCount": 8,
    "documentCount": 25,
    "memberUsernames": ["user1", "user2"],
    "createdAt": "2025-11-15T09:00:00",
    "updatedAt": "2025-12-15T14:30:00"
  }
]
```

### Search with facets (for filter counts)
**POST** `/api/search/facets`

Request body:
```json
{
  "query": "angular",
  "tags": ["Frontend"],
  "page": 0,
  "size": 10
}
```

Response includes facets:
```json
{
  "documents": [...],
  "tagFacets": {
    "Angular": 25,
    "TypeScript": 18,
    "Frontend": 22
  },
  "fileTypeFacets": {
    "PDF": 18,
    "DOCX": 5
  },
  "sharingLevelFacets": {
    "PUBLIC": 20,
    "GROUP": 4
  },
  ...
}
```

---

## CURL Test Commands

```bash
# Get token first
TOKEN=$(curl -s -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# Simple search
curl -X GET "http://localhost:8090/api/search?q=angular" \
  -H "Authorization: Bearer $TOKEN" | jq

# Filter by sharing level
curl -X GET "http://localhost:8090/api/search?sharingLevel=PRIVATE" \
  -H "Authorization: Bearer $TOKEN" | jq

# Search with tags
curl -X GET "http://localhost:8090/api/search?tags=Java,Spring&matchAllTags=true" \
  -H "Authorization: Bearer $TOKEN" | jq

# Search with rating
curl -X GET "http://localhost:8090/api/search?minRating=4.0&sortBy=rating" \
  -H "Authorization: Bearer $TOKEN" | jq

# Search favorites
curl -X GET "http://localhost:8090/api/search?onlyFavorited=true" \
  -H "Authorization: Bearer $TOKEN" | jq

# Complex search
curl -X GET "http://localhost:8090/api/search?q=spring&tags=Java,Backend&fileType=PDF&minRating=4.0&sortBy=rating&page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized - Invalid or missing token",
  "timestamp": "2025-12-22T10:30:00"
}
```

### 400 Bad Request
```json
{
  "status": 400,
  "message": "Invalid request parameters",
  "timestamp": "2025-12-22T10:30:00"
}
```

### 500 Internal Server Error
```json
{
  "status": 500,
  "message": "An unexpected error occurred",
  "timestamp": "2025-12-22T10:30:00"
}
```

---

## Summary

✅ **ONE endpoint for everything:** `/api/search`  
✅ **18 optional parameters** - use only what you need  
✅ **Simple searches:** `?q=angular`  
✅ **Advanced searches:** combine any parameters  
✅ **TypeScript interfaces** provided  
✅ **Angular service** example included  
✅ **All use cases** covered with examples

