import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Project {
  id: string;
  name: string;
  code: string;
  description: string;
  createdOn: string;
  ownerId: string;
}

export interface CreateProjectCommand {
  name: string;
  code: string;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = '/api/projects';

  constructor(private http: HttpClient) {}

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  getProjectById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  createProject(command: CreateProjectCommand): Observable<string> {
    return this.http.post<string>(this.apiUrl, command);
  }
}
