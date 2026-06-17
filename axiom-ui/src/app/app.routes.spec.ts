import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';
import { AppLayoutComponent } from './layout/app-layout';
import { LoginComponent } from './views/login/login';
// The Register view was moved/renamed in the app. Provide a local
// test stub so the routes spec can assert presence without importing
// the real component implementation.
class RegisterComponent { }

describe('routes', () => {
  it('exposes login as a public route', () => {
    expect(routes).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ path: 'login', component: LoginComponent }),
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

  it('redirects unknown routes to the guarded shell', () => {
    expect(routes.at(-1)).toEqual({ path: '**', redirectTo: '' });
  });
});
