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
    id: 'pa',
    name: 'Project Alpha',
    code: 'PA',
    type: 'Software Project',
    colorClass: 'bg-[#0052cc]',
  };

  @Input() projects: DropdownProject[] = [
    {
      id: 'pa',
      name: 'Project Alpha',
      code: 'PA',
      type: 'Software Project',
      colorClass: 'bg-[#0052cc]',
    },
    {
      id: 'pb',
      name: 'Project Beta',
      code: 'PB',
      type: 'Software Project',
      colorClass: 'bg-[#0747a6]',
    },
    {
      id: 'mc',
      name: 'Marketing Campaign',
      code: 'MC',
      type: 'Business',
      colorClass: 'bg-[#8b5cf6]',
    },
    {
      id: 'ma',
      name: 'Mobile App Redesign',
      code: 'MA',
      type: 'Software Project',
      colorClass: 'bg-[#0ea5e9]',
    },
  ];

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
