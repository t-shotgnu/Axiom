import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';
import { AppLayoutComponent } from './layout/app-layout';
import { LoginComponent } from './views/login/login';
import { RegisterComponent } from './views/register/register';

describe('routes', () => {
  it('exposes login and register as public routes', () => {
    expect(routes).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ path: 'login', component: LoginComponent }),
        expect.objectContaining({ path: 'register', component: RegisterComponent }),
      ]),
    );
  });

  it('protects the application shell with the auth guard', () => {
    const shellRoute = routes.find((route) => route.path === '');

    expect(shellRoute).toEqual(
      expect.objectContaining({
        component: AppLayoutComponent,
        canActivate: [authGuard],
      }),
    );
  });

  it('routes shell children to dashboard, projects, and tasks', () => {
    const shellRoute = routes.find((route) => route.path === '');
    const childPaths = shellRoute?.children?.map((route) => route.path);

    expect(childPaths).toEqual([
      '',
      'dashboard',
      'projects',
      'projects/:id',
      'tasks',
      'tasks/:id',
    ]);
  });

  it('redirects unknown routes to the guarded shell', () => {
    expect(routes.at(-1)).toEqual({ path: '**', redirectTo: '' });
  });
});
