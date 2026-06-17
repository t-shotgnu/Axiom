import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';
import { AppLayoutComponent } from './layout/app-layout';

describe('routes', () => {
  it('exposes login as a public route', () => {
    expect(routes).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ path: 'login', loadComponent: expect.any(Function) }),
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
      'projects/:id/settings',
      'tasks',
      'tasks/:id',
      'team',
    ]);
  });

  it('lazy loads feature views under the guarded shell', () => {
    const shellRoute = routes.find((route) => route.path === '');
    const featureChildren = shellRoute?.children?.filter((route) => route.path && route.path !== '');

    expect(featureChildren).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ path: 'dashboard', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'projects', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'projects/:id', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'projects/:id/settings', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'tasks', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'tasks/:id', loadComponent: expect.any(Function) }),
        expect.objectContaining({ path: 'team', loadComponent: expect.any(Function) }),
      ]),
    );
  });

  it('redirects unknown routes to the guarded shell', () => {
    expect(routes.at(-1)).toEqual({ path: '**', redirectTo: '' });
  });
});
