import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layout/app-layout';
import { authGuard } from './core/guards/auth.guard';
import { pendingChangesGuard } from './core/guards/pending-changes.guard';

export const routes: Routes = [
	{
		path: 'login',
		loadComponent: () => import('./views/login/login').then((m) => m.LoginComponent),
	},
	{
		path: '',
		component: AppLayoutComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', redirectTo: 'dashboard', pathMatch: 'full' },
			{
				path: 'dashboard',
				loadComponent: () => import('./views/dashboard/dashboard').then((m) => m.DashboardComponent),
			},
			{
				path: 'projects',
				loadComponent: () => import('./views/projects/projects').then((m) => m.ProjectsComponent),
			},
			{
				path: 'projects/:id',
				loadComponent: () => import('./views/project-detail/project-detail').then((m) => m.ProjectDetailComponent),
			},
			{
				path: 'projects/:id/settings',
				loadComponent: () => import('./views/project-settings/project-settings').then((m) => m.ProjectSettingsComponent),
			},
			{
				path: 'tasks',
				loadComponent: () => import('./views/tasks/tasks').then((m) => m.TasksComponent),
			},
			{
				path: 'tasks/:id',
				loadComponent: () => import('./views/task-detail/task-detail').then((m) => m.TaskDetailComponent),
				canDeactivate: [pendingChangesGuard],
			},
			{
				path: 'team',
				loadComponent: () => import('./views/team/team').then((m) => m.TeamComponent),
			},
		],
	},
	{ path: '**', redirectTo: '' },
];
