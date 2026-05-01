import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, ButtonModule],
  template: `
    <div class="max-w-md mx-auto mt-12">
      <h1 class="text-2xl font-semibold mb-4">Sign in</h1>
      <form class="space-y-3">
        <div>
          <label class="block text-sm mb-1">Email</label>
          <input pInputText type="email" class="w-full" />
        </div>
        <div>
          <label class="block text-sm mb-1">Password</label>
          <input pInputText type="password" class="w-full" />
        </div>
        <div class="flex justify-end">
          <button pButton type="button" label="Sign in"></button>
        </div>
      </form>
    </div>
  `,
})
export class LoginComponent {}
