import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { appConfig } from './app.config';
import { routes } from './app.routes';

describe('appConfig', () => {
  it('wires the application level providers', () => {
    TestBed.configureTestingModule({
      providers: appConfig.providers,
    });

    expect(TestBed.inject(HttpClient)).toBeTruthy();
    expect(TestBed.inject(Router).config).toEqual(routes);
    expect(appConfig.providers).toHaveLength(3);
  });
});
