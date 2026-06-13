import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';

export interface WorkItem {
  id: string;
  controlNo: number;
  description: string;
  priority: number;
  type: string;
  status: string;
  dueDate?: string | null;
  estimatedEffort?: number | null;
  projectId: string;
  authorId: string;
  assigneeId?: string | null;
  notes?: string;
}

export interface CreateWorkItemCommand {
  description: string;
  priority: number;
  type: string;
  status: string;
  dueDate?: string | null;
  estimatedEffort?: number | null;
  projectId: string;
  assigneeId?: string | null;
}

export interface AssignWorkItemCommand {
  assigneeId: string | null;
}

export interface UpdateWorkItemStatusCommand {
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class WorkItemService {
  private apiUrl = '/api/work-items';

  constructor(private http: HttpClient) { }

  getWorkItems(projectId: string): Observable<WorkItem[]> {
    return this.http.get<WorkItem[]>(`${this.apiUrl}?projectId=${projectId}`);
  }

  getWorkItemById(id: string): Observable<WorkItem> {
    return this.http.get<WorkItem>(`${this.apiUrl}/${id}`);
  }

  createWorkItem(command: CreateWorkItemCommand): Observable<string> {
    return this.http.post<string>(this.apiUrl, command);
  }

  assignWorkItem(id: string, command: AssignWorkItemCommand): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/assignee`, command);
  }

  updateWorkItemStatus(id: string, command: UpdateWorkItemStatusCommand): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/status`, command);
  }

  updateWorkItemNotes(id: string, notes: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/notes`, { notes });
  }

  updateWorkItem(id: string, command: Partial<CreateWorkItemCommand & { notes?: string; assigneeId?: string | null }>): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}`, command);
  }

  deleteWorkItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getRelationshipsByProject(projectId: string): Observable<TaskRelationship[]> {
    return this.http.get<TaskRelationship[]>(`/api/task-relationships?projectId=${projectId}`);
  }

  getRelationshipsByWorkItem(workItemId: string): Observable<TaskRelationship[]> {
    return this.http.get<TaskRelationship[]>(`/api/task-relationships/work-item/${workItemId}`);
  }

  createRelationship(command: CreateTaskRelationshipCommand): Observable<string> {
    return this.http.post<string>('/api/task-relationships', command);
  }

  deleteRelationship(id: string): Observable<void> {
    return this.http.delete<void>(`/api/task-relationships/${id}`);
  }

  /**
   * Returns the allowed child types for each work item type, as defined by the backend domain rules.
   * The result is cached for the lifetime of the app (shareReplay) — one HTTP request ever.
   */
  private _typeHierarchy$?: Observable<Record<string, string[]>>;
  getTypeHierarchy(): Observable<Record<string, string[]>> {
    if (!this._typeHierarchy$) {
      this._typeHierarchy$ = this.http
        .get<Record<string, string[]>>(`${this.apiUrl}/type-hierarchy`)
        .pipe(shareReplay(1));
    }
    return this._typeHierarchy$;
  }
}

export interface TaskRelationship {
  id: string;
  sourceId: string;
  targetId: string;
  linkType: string;
}

export interface CreateTaskRelationshipCommand {
  sourceId: string;
  targetId: string;
  linkType: string;
}

