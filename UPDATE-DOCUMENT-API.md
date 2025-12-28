# Update Document API - Angular Integration

## Endpoint
**PUT** `/api/documents/{id}`

**Auth:** Required (Bearer token)

**Content-Type:** `multipart/form-data`

---

## Request

### Path Parameter
- `id` (number) - Document ID

### Form Data (multipart/form-data)

#### Part 1: `data` (JSON)
```json
{
  "title": "Angular Best Practices Updated",
  "summary": "Updated comprehensive guide for Angular development",
  "sharingLevel": "PUBLIC",
  "tags": ["Angular", "TypeScript", "Frontend", "Updated"]
}
```

#### Part 2: `file` (File) - **OPTIONAL**
- Only include if you want to upload a new file version
- Supported types: PDF, DOCX, XLSX, PPTX, TXT, IMAGE
- Max size: 50MB

---

## Request Fields

| Field | Type | Required | Valid Values | Description |
|-------|------|----------|--------------|-------------|
| `title` | string | ✅ Yes | max 255 chars | Document title |
| `summary` | string | ❌ No | max 500 chars | Brief description |
| `sharingLevel` | string | ✅ Yes | `PUBLIC`, `PRIVATE`, `GROUP` | Access level |
| `tags` | string[] | ❌ No | - | Document tags |
| `file` | File | ❌ No | PDF/DOC/Image | New file version |

---

## Response (200 OK)

```json
{
  "id": 5,
  "title": "Angular Best Practices Updated",
  "summary": "Updated comprehensive guide for Angular development",
  "content": null,
  "filePath": "79667fc7-c918-449b-8410-e19a52d6a113.pdf",
  "fileType": "PDF",
  "fileSize": 331134,
  "sharingLevel": "PUBLIC",
  "versionNumber": 2,
  "isArchived": false,
  "ownerId": 5,
  "ownerUsername": "nguyenvu12",
  "averageRating": 4.5,
  "ratingCount": 3,
  "createdAt": "2025-12-22T16:14:39.469599",
  "updatedAt": "2025-12-23T10:30:00.123456",
  "tags": ["Angular", "TypeScript", "Frontend", "Updated"],
  "groupIds": []
}
```

---

## Angular Service Example

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UpdateDocumentRequest {
  title: string;
  summary?: string;
  sharingLevel: 'PUBLIC' | 'PRIVATE' | 'GROUP';
  tags?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = 'http://localhost:8090/api/documents';

  constructor(private http: HttpClient) {}

  /**
   * Update document metadata and optionally upload new file
   */
  updateDocument(
    id: number, 
    data: UpdateDocumentRequest, 
    file?: File
  ): Observable<DocumentResponse> {
    const formData = new FormData();
    
    // Add JSON data as blob
    const dataBlob = new Blob([JSON.stringify(data)], {
      type: 'application/json'
    });
    formData.append('data', dataBlob);
    
    // Add file if provided
    if (file) {
      formData.append('file', file);
    }
    
    return this.http.put<DocumentResponse>(
      `${this.apiUrl}/${id}`, 
      formData
    );
  }
}
```

---

## Component Example

```typescript
export class EditDocumentComponent {
  updateDocument() {
    const data: UpdateDocumentRequest = {
      title: this.form.value.title,
      summary: this.form.value.summary,
      sharingLevel: this.form.value.sharingLevel,
      tags: this.form.value.tags
    };

    const file = this.selectedFile; // From file input

    this.documentService.updateDocument(this.documentId, data, file)
      .subscribe({
        next: (response) => {
          console.log('Document updated:', response);
          this.router.navigate(['/documents', response.id]);
        },
        error: (error) => {
          console.error('Update failed:', error);
        }
      });
  }
}
```

---

## CURL Example

### Update metadata only (no file)
```bash
curl -X PUT "http://localhost:8090/api/documents/5" \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"title":"Updated Title","summary":"Updated summary","sharingLevel":"PUBLIC","tags":["Angular","Updated"]};type=application/json'
```

### Update with new file
```bash
curl -X PUT "http://localhost:8090/api/documents/5" \
  -H "Authorization: Bearer $TOKEN" \
  -F 'data={"title":"Updated Title","summary":"Updated summary","sharingLevel":"PUBLIC","tags":["Angular","Updated"]};type=application/json' \
  -F 'file=@/path/to/newfile.pdf'
```

---

## Error Responses

### 400 Bad Request - Invalid data
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-12-23T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized - Invalid or missing token",
  "timestamp": "2025-12-23T10:30:00"
}
```

### 403 Forbidden - Not owner
```json
{
  "status": 403,
  "message": "You don't have permission to update this document",
  "timestamp": "2025-12-23T10:30:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Document not found with id: 5",
  "timestamp": "2025-12-23T10:30:00"
}
```

---

## Important Notes

1. **Version Number**: Automatically increments when file is updated
2. **File Optional**: Can update metadata without changing file
3. **Owner Only**: Only document owner can update
4. **Timestamp**: `updatedAt` automatically updated
5. **Tags**: Replace all existing tags (not merge)

---

## Testing with Postman

1. **Set Method**: PUT
2. **URL**: `http://localhost:8090/api/documents/5`
3. **Headers**: 
   - `Authorization: Bearer {your_token}`
4. **Body** → Select `form-data`:
   - Key: `data`, Type: `Text`, Value: `{"title":"Updated","summary":"Test","sharingLevel":"PUBLIC","tags":["Test"]}`
   - Key: `file`, Type: `File`, Value: Select file (optional)
5. **Send**

---

## Summary

✅ **Endpoint**: `PUT /api/documents/{id}`  
✅ **Content-Type**: `multipart/form-data`  
✅ **Required**: `data` (JSON blob)  
✅ **Optional**: `file` (File)  
✅ **Auth**: Bearer token required  
✅ **Returns**: Updated `DocumentResponse`  
✅ **Version**: Auto-increments when file changed

