import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, CardModule],
  templateUrl: './projects.html',
})
export class ProjectsComponent {}
