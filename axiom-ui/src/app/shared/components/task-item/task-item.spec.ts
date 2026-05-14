import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { WorkItem } from '../../../core/services/work-item.service';
import { TaskItemComponent } from './task-item';

describe('TaskItemComponent', () => {
  const task: WorkItem = {
    id: 'task-1',
    controlNo: 42,
    description: 'Ship tests',
    priority: 2,
    type: 'Task',
    status: 'Active',
    dueDate: '',
    estimatedEffort: 1,
    projectId: 'project-1',
    authorId: 'user-1',
    assigneeId: '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskItemComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  function createComponent(linkEnabled = true): ComponentFixture<TaskItemComponent> {
    const fixture = TestBed.createComponent(TaskItemComponent);
    fixture.componentRef.setInput('task', task);
    fixture.componentRef.setInput('linkEnabled', linkEnabled);
    fixture.detectChanges();
    return fixture;
  }

  it('renders the task control number and description', () => {
    const fixture = createComponent();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('#42');
    expect(element.textContent).toContain('Ship tests');
  });

  it('enables link styling by default', () => {
    const fixture = createComponent();
    const anchor = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(anchor.classList.contains('cursor-pointer')).toBe(true);
  });

  it('disables link styling when linkEnabled is false', () => {
    const fixture = createComponent(false);
    const anchor = fixture.nativeElement.querySelector('a') as HTMLAnchorElement;

    expect(anchor.classList.contains('cursor-pointer')).toBe(false);
  });

  it('maps task statuses to tag severities', () => {
    const fixture = createComponent();
    const component = fixture.componentInstance;

    expect(component.getSeverity('New')).toBe('info');
    expect(component.getSeverity('Active')).toBe('warn');
    expect(component.getSeverity('Resolved')).toBe('success');
    expect(component.getSeverity('Closed')).toBe('secondary');
    expect(component.getSeverity('Unexpected')).toBe('info');
  });
});
