import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ProjectDto {
  id: string;
  name: string;
  code: string;
  description: string | null;
  createdOn: string;
  ownerId: string;
}

export interface CreateProjectRequest {
  name: string;
  code: string;
  description: string | null;
  ownerId: string;
}

@Injectable({ providedIn: 'root' })
export class ProjectsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/projects';

  getProjects(): Observable<ProjectDto[]> {
    return this.http.get<ProjectDto[]>(this.baseUrl);
  }

  createProject(project: CreateProjectRequest): Observable<string> {
    return this.http.post<string>(this.baseUrl, project);
  }
}
