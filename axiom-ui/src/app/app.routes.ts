import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layout/app-layout';
import { DashboardComponent } from './views/dashboard/dashboard';
import { ProjectsComponent } from './views/projects/projects';
import { TasksComponent } from './views/tasks/tasks';
import { TaskDetailComponent } from './views/task-detail/task-detail';
import { LoginComponent } from './views/login/login';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{
		path: '',
		component: AppLayoutComponent,
		children: [
			{ path: '', redirectTo: 'dashboard', pathMatch: 'full' },
			{ path: 'dashboard', component: DashboardComponent },
			{ path: 'projects', component: ProjectsComponent },
			{ path: 'tasks', component: TasksComponent },
			{ path: 'tasks/:id', component: TaskDetailComponent },
		],
	},
	{ path: '**', redirectTo: '' },
];
