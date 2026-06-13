import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Attachment {
  id: string;
  workItemId: string;
  uploadedBy: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  uploadedOn: string;
}

@Injectable({
  providedIn: 'root'
})
export class AttachmentService {
  private readonly http = inject(HttpClient);

  getAttachments(workItemId: string): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(`/api/work-items/${workItemId}/attachments`);
  }

  uploadAttachment(workItemId: string, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<string>(`/api/work-items/${workItemId}/attachments`, formData);
  }

  downloadAttachment(id: string): Observable<Blob> {
    return this.http.get(`/api/attachments/${id}/download`, { responseType: 'blob' });
  }

  deleteAttachment(id: string): Observable<void> {
    return this.http.delete<void>(`/api/attachments/${id}`);
  }
}
