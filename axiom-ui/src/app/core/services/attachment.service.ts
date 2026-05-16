import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Attachment {
  id: string;
  workItemId: string;
  fileName: string;
  fileSize: string;
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
}
