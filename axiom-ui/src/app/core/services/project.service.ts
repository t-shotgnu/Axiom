import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Project {
  id: string;
  name: string;
  code: string;
  description: string;
  createdOn: string;
  ownerId: string;
  ownerName?: string;
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

  private readonly currentProjectSubject = new BehaviorSubject<Project | null>(null);
  readonly currentProject$ = this.currentProjectSubject.asObservable();

  constructor(private http: HttpClient) { }

  setCurrentProject(project: Project | null): void {
    this.currentProjectSubject.next(project);
  }

  getCurrentProject(): Project | null {
    return this.currentProjectSubject.value;
  }

  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl);
  }

  getProjectById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  createProject(command: CreateProjectCommand): Observable<string> {
    return this.http.post<string>(this.apiUrl, command);
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
