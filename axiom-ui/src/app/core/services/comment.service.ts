import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comment {
  id: string;
  workItemId: string;
  author: string;
  text: string;
  createdOn: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private readonly http = inject(HttpClient);

  getComments(workItemId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`/api/work-items/${workItemId}/comments`);
  }

  addComment(workItemId: string, text: string): Observable<Comment> {
    return this.http.post<Comment>(`/api/work-items/${workItemId}/comments`, { text });
  }

  deleteComment(id: string): Observable<void> {
    return this.http.delete<void>(`/api/comments/${id}`);
  }
}
