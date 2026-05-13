import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

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

  getProjects() {
    return this.http.get<ProjectDto[]>(this.baseUrl);
  }

  createProject(project: CreateProjectRequest) {
    return this.http.post<string>(this.baseUrl, project);
  }
}
