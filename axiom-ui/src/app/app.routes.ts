import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layout/app-layout';
import { DashboardComponent } from './views/dashboard/dashboard';
import { ProjectsComponent } from './views/projects/projects';
import { TasksComponent } from './views/tasks/tasks';
import { TaskDetailComponent } from './views/task-detail/task-detail';
import { LoginComponent } from './views/login/login';
import { ProjectDetailComponent } from './views/project-detail/project-detail';
import { ProjectSettingsComponent } from './views/project-settings/project-settings';
import { TeamComponent } from './views/team/team';
import { authGuard } from './core/guards/auth.guard';
import { pendingChangesGuard } from './core/guards/pending-changes.guard';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{
		path: '',
		component: AppLayoutComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', redirectTo: 'dashboard', pathMatch: 'full' },
			{ path: 'dashboard', component: DashboardComponent },
			{ path: 'projects', component: ProjectsComponent },
			{ path: 'projects/:id', component: ProjectDetailComponent },
			{ path: 'projects/:id/settings', component: ProjectSettingsComponent },
			{ path: 'tasks', component: TasksComponent },
			{ path: 'tasks/:id', component: TaskDetailComponent, canDeactivate: [pendingChangesGuard] },
			{ path: 'team', component: TeamComponent },
		],
	},
	{ path: '**', redirectTo: '' },
];
