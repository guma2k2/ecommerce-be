# Media API Integration Guide

This document provides API specifications, data models, error codes, and TypeScript / Axios integration examples for Frontend (FE) developers integrating the **Media Management & Upload Service**.

---

## 1. Overview & Architecture

### Base URL
- **Local / Dev**: `http://localhost:8080`
- **Base Path**: `/api/v1/medias`

### Media Type Auto-Detection
- Uploaded files are automatically identified as either `IMAGE` or `VIDEO` based on MIME type (e.g., `image/*`, `video/*`) and file extensions (e.g., `.png`, `.jpg`, `.webp`, `.mp4`, `.mov`).
- **No manual type parameter is required** in the request.
- Unsupported file types result in an HTTP 400 `INVALID_MEDIA_TYPE` error.

### Unified Response Format
All responses adhere to the standard `ApiResponse<T>` envelope:
```json
{
  "status": "200",
  "message": "success",
  "data": { ... }
}
```

---

## 2. Media Upload & Retrieval Flow

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant FE as Frontend Application
    participant BE as Backend API (/api/v1/medias)
    participant Storage as File Storage (Local / Cloudinary)
    participant DB as PostgreSQL Database

    User->>FE: Select file & enter Alt Text
    FE->>BE: POST /api/v1/medias (multipart/form-data with file & altText)
    BE->>BE: Detect MediaType (IMAGE / VIDEO) & File Extension
    alt Invalid/Unsupported File Type
        BE-->>FE: 400 Bad Request (INVALID_MEDIA_TYPE)
        FE-->>User: Display error message
    else Valid File Type
        BE->>Storage: Save file / stream bytes
        Storage-->>BE: Stored File URL & metadata
        BE->>DB: Save Media record (name, url, type, size, etc.)
        DB-->>BE: Saved Media Entity
        BE-->>FE: 200 OK with MediaResponse
        FE-->>User: Display uploaded media / preview
    end
```

---

## 3. Data Models & TypeScript Interfaces

### TypeScript Types
```typescript
export type MediaType = 'IMAGE' | 'VIDEO';

export interface MediaResponse {
  id: string;
  name: string;
  url: string;
  type: MediaType;
  size: number;
  altText: string;
  fileType: string;
  active: boolean;
  duration?: string | null;
}

export interface PageResponse<T> {
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  content: T[];
}

export interface ApiResponse<T> {
  status: string;
  message: string;
  data: T;
}
```

---

## 4. API Endpoints Specification

### 4.1 Upload Media
Uploads an image or video file. Media type is automatically detected by the backend.

- **URL**: `/api/v1/medias`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Authentication**: `Bearer <token>` (if authentication is enabled)

#### Request Parameters (Form Data)
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `file` | `File` / Binary | **Yes** | Image or video file to upload. |
| `altText` | `string` | No | Description or alt text for accessibility/SEO (defaults to file name if omitted). |

#### Supported Formats
- **Images**: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`, `.bmp`, `.svg`, `.tiff`, `.ico`, `.avif`, `.heic`, `.heif`
- **Videos**: `.mp4`, `.mov`, `.avi`, `.mkv`, `.flv`, `.wmv`, `.webm`, `.m4v`, `.3gp`, `.ts`, `.mpg`, `.mpeg`

#### Success Response (`200 OK`)
```json
{
  "status": "200",
  "message": "success",
  "data": {
    "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "name": "banner.png",
    "url": "http://localhost:8080/uploads/3a6e9a66-1c4b-4b2b-980b-f3508d0e5138.png",
    "type": "IMAGE",
    "size": 245892,
    "altText": "Summer Sale Banner",
    "fileType": "png",
    "active": true,
    "duration": null
  }
}
```

#### Error Response (`400 Bad Request`)
```json
{
  "status": "400",
  "message": "Invalid media type: application/pdf",
  "data": null
}
```

---

### 4.2 Get Paginated Media List
Retrieves a paginated list of all uploaded media records.

- **URL**: `/api/v1/medias/page`
- **Method**: `GET`
- **Content-Type**: `application/json`

#### Query Parameters
| Parameter | Type | Required | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `pageNumber` | integer | No | `0` | Zero-based page index. |
| `pageSize` | integer | No | `10` | Number of items per page. |

#### Success Response (`200 OK`)
```json
{
  "status": "200",
  "message": "success",
  "data": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 3,
    "totalElements": 25,
    "content": [
      {
        "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "name": "banner.png",
        "url": "http://localhost:8080/uploads/3a6e9a66-1c4b-4b2b-980b-f3508d0e5138.png",
        "type": "IMAGE",
        "size": 245892,
        "altText": "Summer Sale Banner",
        "fileType": "png",
        "active": true,
        "duration": null
      },
      {
        "id": "4c8f5e12-8e2b-42ab-8c9a-9e1f3d6b7c8a",
        "name": "product-demo.mp4",
        "url": "https://res.cloudinary.com/demo/video/upload/v123456/sample.mp4",
        "type": "VIDEO",
        "size": 5242880,
        "altText": "Product Feature Video",
        "fileType": "mp4",
        "active": true,
        "duration": "15.4"
      }
    ]
  }
}
```

---

### 4.3 Get Media by ID
Fetches details of a single media item by its UUID.

- **URL**: `/api/v1/medias/{mediaId}`
- **Method**: `GET`
- **Content-Type**: `application/json`

#### Path Variables
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `mediaId` | string (UUID) | **Yes** | The unique identifier of the media. |

#### Success Response (`200 OK`)
```json
{
  "status": "200",
  "message": "success",
  "data": {
    "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "name": "banner.png",
    "url": "http://localhost:8080/uploads/3a6e9a66-1c4b-4b2b-980b-f3508d0e5138.png",
    "type": "IMAGE",
    "size": 245892,
    "altText": "Summer Sale Banner",
    "fileType": "png",
    "active": true,
    "duration": null
  }
}
```

#### Error Response (`404 Not Found`)
```json
{
  "status": "404",
  "message": "Media not found",
  "data": null
}
```

---

## 5. Frontend Code Examples

### 5.1 Axios Implementation Service

```typescript
import axios, { AxiosProgressEvent } from 'axios';
import { ApiResponse, MediaResponse, PageResponse } from './types';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  withCredentials: true,
});

export const mediaApi = {
  /**
   * Uploads an image or video file.
   * Media type is automatically detected by the backend.
   */
  uploadMedia: async (
    file: File,
    altText?: string,
    onUploadProgress?: (progressEvent: AxiosProgressEvent) => void
  ): Promise<MediaResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    if (altText && altText.trim().length > 0) {
      formData.append('altText', altText.trim());
    }

    const response = await apiClient.post<ApiResponse<MediaResponse>>('/medias', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    });

    return response.data.data;
  },

  /**
   * Fetches paginated list of media files.
   */
  getMediaPage: async (
    pageNumber: number = 0,
    pageSize: number = 10
  ): Promise<PageResponse<MediaResponse>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<MediaResponse>>>('/medias/page', {
      params: { pageNumber, pageSize },
    });
    return response.data.data;
  },

  /**
   * Fetches a media record by UUID.
   */
  getMediaById: async (mediaId: string): Promise<MediaResponse> => {
    const response = await apiClient.get<ApiResponse<MediaResponse>>(`/medias/${mediaId}`);
    return response.data.data;
  },
};
```

---

### 5.2 React Upload Component Example

```tsx
import React, { useState } from 'react';
import { mediaApi } from './mediaApi';
import { MediaResponse } from './types';

export const MediaUploader: React.FC = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [altText, setAltText] = useState('');
  const [progress, setProgress] = useState<number>(0);
  const [uploadedMedia, setUploadedMedia] = useState<MediaResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setError(null);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setError('Please select a file to upload.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setProgress(0);

      const result = await mediaApi.uploadMedia(selectedFile, altText, (event) => {
        if (event.total) {
          const percent = Math.round((event.loaded * 100) / event.total);
          setProgress(percent);
        }
      });

      setUploadedMedia(result);
      setSelectedFile(null);
      setAltText('');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to upload media.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 480, margin: '2rem auto', padding: '1.5rem', border: '1px solid #ddd', borderRadius: 8 }}>
      <h2>Upload Media</h2>
      <form onSubmit={handleUpload}>
        <div style={{ marginBottom: '1rem' }}>
          <label>Choose File (Image or Video):</label>
          <input
            type="file"
            accept="image/*,video/*"
            onChange={handleFileChange}
            disabled={loading}
            style={{ display: 'block', marginTop: 8 }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label>Alt Text (Optional):</label>
          <input
            type="text"
            value={altText}
            placeholder="Descriptive alt text for SEO"
            onChange={(e) => setAltText(e.target.value)}
            disabled={loading}
            style={{ width: '100%', padding: '0.5rem', marginTop: 8 }}
          />
        </div>

        {loading && (
          <div style={{ marginBottom: '1rem' }}>
            <progress value={progress} max="100" style={{ width: '100%' }} />
            <span>{progress}%</span>
          </div>
        )}

        {error && <p style={{ color: 'red', margin: '0.5rem 0' }}>{error}</p>}

        <button type="submit" disabled={loading || !selectedFile} style={{ padding: '0.5rem 1.5rem' }}>
          {loading ? 'Uploading...' : 'Upload'}
        </button>
      </form>

      {uploadedMedia && (
        <div style={{ marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid #eee' }}>
          <h3>Uploaded Successfully</h3>
          <p><strong>ID:</strong> {uploadedMedia.id}</p>
          <p><strong>Type:</strong> {uploadedMedia.type}</p>
          <p><strong>URL:</strong> <a href={uploadedMedia.url} target="_blank" rel="noreferrer">{uploadedMedia.url}</a></p>
          
          {uploadedMedia.type === 'IMAGE' ? (
            <img src={uploadedMedia.url} alt={uploadedMedia.altText} style={{ maxWidth: '100%', borderRadius: 4 }} />
          ) : (
            <video src={uploadedMedia.url} controls style={{ maxWidth: '100%', borderRadius: 4 }} />
          )}
        </div>
      )}
    </div>
  );
};
```

---

## 6. Error Codes Reference

| Error Code | HTTP Status | Description | User Action / Resolution |
| :--- | :--- | :--- | :--- |
| `INVALID_MEDIA_TYPE` | `400 Bad Request` | The uploaded file is neither an image nor a video. | Ensure the file has a supported extension/MIME type. |
| `MEDIA_NOT_FOUND` | `404 Not Found` | No media record matches the provided `mediaId`. | Verify the `mediaId` UUID string. |
| `INTERNAL_SERVER_ERROR` | `500 Server Error` | File storage or server I/O error occurred. | Retry upload or check backend storage logs. |
