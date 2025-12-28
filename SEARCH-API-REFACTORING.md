# Search API Refactoring - Unified Single Endpoint

## 🎯 TẠI SAO GỘP CHUNG THÀNH 1 ENDPOINT?

### ❌ Vấn Đề Trước Khi Refactor:

Trước đây có **4 endpoints khác nhau** làm **CÙNG 1 VIỆC**:
1. `GET /api/search` - "Quick search" 
2. `GET /api/search/advanced` - "Advanced search"
3. `GET /api/search/by-tags` - "Tag search"
4. `GET /api/search/favorites` - "Favorites search"

**Tất cả đều gọi cùng 1 service method**: `searchService.advancedSearch()`

#### Hệ quả:
- ❌ **Code duplicate**: 4 methods làm cùng 1 việc
- ❌ **Confusing cho developers**: "Tôi nên dùng endpoint nào?"
- ❌ **Frontend phức tạp**: Phải nhớ 4 endpoints, 4 methods
- ❌ **Khó maintain**: Thêm 1 filter phải sửa 4 chỗ
- ❌ **Không RESTful**: Dùng URL path thay vì query params
- ❌ **Testing phức tạp**: Phải test 4 endpoints

### ✅ Sau Khi Refactor:

**CHỈ CÒN 1 ENDPOINT DUY NHẤT:**
```
GET /api/search
```

**Tất cả parameters đều OPTIONAL** → Tự động support cả simple và advanced search!

---

## 📋 SO SÁNH TRƯỚC & SAU

### ❌ TRƯỚC (4 endpoints):

```bash
# Quick search
GET /api/search?q=angular

# Advanced search  
GET /api/search/advanced?q=spring&minRating=4.0&fromDate=2025-01-01

# Tag search
GET /api/search/by-tags?tags=Java,Spring&matchAll=true

# Favorites search
GET /api/search/favorites?q=angular
```

**Frontend phải viết 4 methods:**
```typescript
quickSearch(q: string) { ... }
advancedSearch(filters: AdvancedFilters) { ... }
searchByTags(tags: string[]) { ... }
searchFavorites(q: string) { ... }
```

### ✅ SAU (1 endpoint):

```bash
# Simple search - vẫn NGẮN GỌN như cũ
GET /api/search?q=angular

# Advanced search - CHỈ THÊM params cần thiết
GET /api/search?q=spring&minRating=4.0&fromDate=2025-01-01

# Tag search - CÙNG endpoint
GET /api/search?tags=Java,Spring&matchAllTags=true

# Favorites search - CÙNG endpoint
GET /api/search?onlyFavorited=true&q=angular
```

**Frontend CHỈ CẦN 1 method:**
```typescript
search(filters: SearchFilters) {
  return this.http.get('/api/search', { params: filters });
}
```

---

## 🚀 LỢI ÍCH

### 1. **Đơn Giản Hóa Frontend**
```typescript
// TRƯỚC: 4 methods khác nhau
searchService.quickSearch('angular')
searchService.advancedSearch({ minRating: 4.0, ... })
searchService.searchByTags(['Java', 'Spring'])
searchService.searchFavorites('angular')

// SAU: 1 method duy nhất
searchService.search({ q: 'angular' })
searchService.search({ minRating: 4.0, ... })
searchService.search({ tags: ['Java', 'Spring'] })
searchService.search({ onlyFavorited: true, q: 'angular' })
```

### 2. **Dễ Maintain**
- Thêm 1 filter mới? → Chỉ thêm 1 parameter ở 1 chỗ
- Fix bug? → Chỉ sửa 1 method thay vì 4
- Test? → Chỉ test 1 endpoint với nhiều scenarios

### 3. **RESTful Best Practice**
```
✅ ĐÚNG: GET /api/search?tags=Java&minRating=4.0
❌ SAI:  GET /api/search/by-tags/java/min-rating/4
```

Query parameters là cách ĐÚNG để filter/search trong REST API.

### 4. **Flexible**
User có thể tự do kết hợp BẤT KỲ filters nào:
```bash
# Kết hợp bất kỳ
GET /api/search?q=spring&tags=Java&sharingLevel=PUBLIC&minRating=4.0&fileType=PDF

# Chỉ 1 filter
GET /api/search?sharingLevel=PRIVATE

# Không filter gì (lấy all)
GET /api/search
```

### 5. **Auto-documenting**
OpenAPI/Swagger tự động generate doc cho TẤT CẢ combinations.

---

## 📖 CÁC TRƯỜNG HỢP SỬ DỤNG

### 1. Simple Search (như search bar trên header)
```bash
GET /api/search?q=angular
```
✅ Ngắn gọn như "quick search" cũ

### 2. Filter by Sharing Level
```bash
GET /api/search?sharingLevel=PUBLIC
GET /api/search?sharingLevel=PRIVATE
```

### 3. Filter by File Type
```bash
GET /api/search?fileType=PDF
GET /api/search?fileType=IMAGE
```

### 4. Search by Tags (OR logic)
```bash
GET /api/search?tags=Java,Spring,Backend
```
Tìm documents có **ÍT NHẤT 1** trong các tags

### 5. Search by Tags (AND logic)
```bash
GET /api/search?tags=Java,Spring&matchAllTags=true
```
Tìm documents có **TẤT CẢ** các tags

### 6. Search with Rating Filter
```bash
GET /api/search?minRating=4.0
GET /api/search?minRating=4.0&maxRating=5.0
```

### 7. Search with Date Range
```bash
GET /api/search?fromDate=2025-01-01T00:00:00&toDate=2025-12-31T23:59:59
```

### 8. Search by Owner
```bash
GET /api/search?ownerUsername=admin
GET /api/search?ownerId=5
```

### 9. Search in Specific Groups
```bash
GET /api/search?groupIds=1,2,3&sharingLevel=GROUP
```

### 10. Search Only Favorites
```bash
GET /api/search?onlyFavorited=true
GET /api/search?onlyFavorited=true&q=angular
```

### 11. Include Archived Documents
```bash
GET /api/search?includeArchived=true
```

### 12. Complex Combined Search
```bash
GET /api/search?q=spring&tags=Java,Backend&matchAllTags=true&sharingLevel=PUBLIC&fileType=PDF&minRating=4.0&fromDate=2025-01-01T00:00:00&sortBy=rating&sortOrder=desc&page=0&size=20
```

---

## 🔄 MIGRATION GUIDE (Cho Frontend)

### Angular Service - Trước:
```typescript
export class SearchService {
  quickSearch(query: string, page: number = 0, size: number = 10) {
    return this.http.get<SearchResultResponse>('/api/search', {
      params: { q: query, page, size }
    });
  }

  advancedSearch(filters: AdvancedFilters) {
    return this.http.get<SearchResultResponse>('/api/search/advanced', {
      params: { ...filters }
    });
  }

  searchByTags(tags: string[], matchAll: boolean = false) {
    return this.http.get<SearchResultResponse>('/api/search/by-tags', {
      params: { tags: tags.join(','), matchAll }
    });
  }

  searchFavorites(query?: string) {
    return this.http.get<SearchResultResponse>('/api/search/favorites', {
      params: { q: query || '' }
    });
  }
}
```

### Angular Service - Sau (CHỈ 1 METHOD):
```typescript
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

export class SearchService {
  search(filters: SearchFilters = {}) {
    // Remove undefined/null values
    const params = Object.entries(filters)
      .filter(([_, value]) => value !== undefined && value !== null)
      .reduce((acc, [key, value]) => {
        if (Array.isArray(value)) {
          acc[key] = value.join(',');
        } else {
          acc[key] = value.toString();
        }
        return acc;
      }, {} as any);

    return this.http.get<SearchResultResponse>('/api/search', { params });
  }
}
```

### Component - Trước:
```typescript
// Quick search
this.searchService.quickSearch('angular').subscribe(...)

// Advanced search
this.searchService.advancedSearch({
  query: 'spring',
  minRating: 4.0,
  fileType: 'PDF'
}).subscribe(...)

// Tag search
this.searchService.searchByTags(['Java', 'Spring'], true).subscribe(...)

// Favorites
this.searchService.searchFavorites('angular').subscribe(...)
```

### Component - Sau (ĐỒNG NHẤT):
```typescript
// Quick search
this.searchService.search({ q: 'angular' }).subscribe(...)

// Advanced search
this.searchService.search({
  q: 'spring',
  minRating: 4.0,
  fileType: 'PDF'
}).subscribe(...)

// Tag search
this.searchService.search({ 
  tags: ['Java', 'Spring'], 
  matchAllTags: true 
}).subscribe(...)

// Favorites
this.searchService.search({ 
  onlyFavorited: true, 
  q: 'angular' 
}).subscribe(...)
```

---

## 🧪 TESTING

### Trước: Phải test 4 endpoints
```java
@Test void testQuickSearch() { ... }
@Test void testAdvancedSearch() { ... }
@Test void testSearchByTags() { ... }
@Test void testSearchFavorites() { ... }
```

### Sau: Test 1 endpoint với nhiều scenarios
```java
@Test void testSearch_withKeyword() { ... }
@Test void testSearch_withTags() { ... }
@Test void testSearch_withRating() { ... }
@Test void testSearch_withDateRange() { ... }
@Test void testSearch_withMultipleFilters() { ... }
@Test void testSearch_onlyFavorites() { ... }
```

---

## 📊 METRICS

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Endpoints | 4 | 1 | **-75%** |
| Controller methods | 4 | 1 | **-75%** |
| Frontend service methods | 4 | 1 | **-75%** |
| Lines of code | ~200 | ~120 | **-40%** |
| Test cases needed | 16 | 6 | **-62%** |

---

## ⚠️ BREAKING CHANGES

Các endpoints sau **BỊ XÓA** (deprecated):
- ❌ `GET /api/search/advanced` → Dùng `GET /api/search`
- ❌ `GET /api/search/by-tags` → Dùng `GET /api/search?tags=...`
- ❌ `GET /api/search/favorites` → Dùng `GET /api/search?onlyFavorited=true`

**Lưu ý**: 
- Endpoint `GET /api/search` vẫn hoạt động y hệt như cũ cho simple searches
- Chỉ cần thêm parameters khi cần advanced features

---

## 🎓 BEST PRACTICES

### DO ✅
```bash
# Use query parameters for filtering
GET /api/search?tags=Java&minRating=4.0

# All parameters optional - clean URLs for simple searches
GET /api/search?q=angular

# Combine any filters you need
GET /api/search?onlyFavorited=true&fileType=PDF
```

### DON'T ❌
```bash
# Don't use path segments for filters
GET /api/search/by-tag/Java/min-rating/4.0

# Don't create separate endpoints for every combination
GET /api/search/favorites-with-tag-java-and-min-rating-4
```

---

## 🔗 RELATED ENDPOINTS

Vẫn giữ nguyên:
- ✅ `POST /api/search/facets` - Search with facet counts (for filter UI)
- ✅ `GET /api/tags` - Get all tags
- ✅ `GET /api/groups` - Get all groups

---

## 📝 SUMMARY

**Trước:**
- 4 endpoints
- 4 service methods  
- 4 frontend methods
- Code duplicate
- Confusing
- Hard to maintain

**Sau:**
- 1 endpoint
- 1 service method
- 1 frontend method
- DRY (Don't Repeat Yourself)
- Clear
- Easy to maintain
- RESTful
- Flexible

**👉 Kết luận**: Một API tốt không cần nhiều endpoints, mà cần parameters linh hoạt!

