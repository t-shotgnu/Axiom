import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WorkItem {
  id: string;
  controlNo: number;
  description: string;
  priority: number;
  type: string;
  status: string;
  dueDate: string;
  estimatedEffort: number;
  projectId: string;
  authorId: string;
  assigneeId: string;
}

export interface CreateWorkItemCommand {
  description: string;
  priority: number;
  type: string;
  status: string;
  dueDate?: string;
  estimatedEffort?: number;
  projectId: string;
  assigneeId?: string;
}

export interface AssignWorkItemCommand {
  assigneeId: string;
}

export interface UpdateWorkItemStatusCommand {
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class WorkItemService {
  private apiUrl = '/api/work-items';

  constructor(private http: HttpClient) {}

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
}
