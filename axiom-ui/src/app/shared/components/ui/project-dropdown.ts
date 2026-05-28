import {
  Component,
  Input,
  Output,
  EventEmitter,
  HostListener,
  OnInit,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface DropdownProject {
  id: string;
  name: string;
  code: string;
  type: string;
  colorClass?: string;
}

@Component({
  selector: 'app-project-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './project-dropdown.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProjectDropdownComponent implements OnInit {
  @Input() activeProject: DropdownProject = {
    id: '',
    name: 'No projects',
    code: 'AX',
    type: 'Organization',
    colorClass: 'bg-primary',
  };

  @Input() projects: DropdownProject[] = [];

  @Output() projectSelected = new EventEmitter<DropdownProject>();
  @Output() createProject = new EventEmitter<void>();

  protected isOpen = false;
  protected searchText = '';
  protected filteredProjects: DropdownProject[] = [];

  ngOnInit(): void {
    this.filteredProjects = this.projects;
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.searchText = '';
      this.filteredProjects = this.projects;
    }
  }

  selectProject(proj: DropdownProject): void {
    this.activeProject = proj;
    this.projectSelected.emit(proj);
    this.isOpen = false;
  }

  triggerCreateProject(): void {
    this.createProject.emit();
    this.isOpen = false;
  }

  onSearchChange(): void {
    if (!this.searchText.trim()) {
      this.filteredProjects = this.projects;
      return;
    }
    const q = this.searchText.toLowerCase();
    this.filteredProjects = this.projects.filter(
      (p) =>
        p.name.toLowerCase().includes(q) ||
        p.code.toLowerCase().includes(q) ||
        p.type.toLowerCase().includes(q),
    );
  }

  @HostListener('document:click')
  closeDropdown(): void {
    this.isOpen = false;
  }
}
