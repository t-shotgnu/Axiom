import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProjectMember {
    userId: string;
    userName: string;
    emailAddress: string;
    firstName?: string;
    lastName?: string;
    role: 'MEMBER' | 'ADMIN';
    createdOn: string;
}

export interface AddProjectMemberCommand {
    userId: string;
    role: 'MEMBER' | 'ADMIN';
}

export interface ChangeProjectMemberRoleCommand {
    role: 'MEMBER' | 'ADMIN';
}

@Injectable({
    providedIn: 'root',
})
export class ProjectMemberService {
    constructor(private readonly http: HttpClient) { }

    getProjectMembers(projectId: string): Observable<ProjectMember[]> {
        return this.http.get<ProjectMember[]>(`/api/projects/${projectId}/members`);
    }

    addProjectMember(projectId: string, command: AddProjectMemberCommand): Observable<void> {
        return this.http.post<void>(`/api/projects/${projectId}/members`, command);
    }

    changeProjectMemberRole(
        projectId: string,
        userId: string,
        command: ChangeProjectMemberRoleCommand,
    ): Observable<void> {
        return this.http.patch<void>(`/api/projects/${projectId}/members/${userId}`, command);
    }

    removeProjectMember(projectId: string, userId: string): Observable<void> {
        return this.http.delete<void>(`/api/projects/${projectId}/members/${userId}`);
    }
}