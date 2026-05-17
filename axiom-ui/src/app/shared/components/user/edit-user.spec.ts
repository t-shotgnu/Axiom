import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { EditUserComponent } from './edit-user';
import { UserService, User } from '../../../core/services/user.service';

describe('EditUserComponent', () => {
    let fixture: ComponentFixture<EditUserComponent>;
    let mockUserService: Partial<UserService>;

    beforeEach(async () => {
        mockUserService = {
            getCurrentUserProfile: () => of({
                id: 'u1',
                userName: 'user',
                emailAddress: 'user@example.com',
                firstName: 'First',
                lastName: 'Last',
                dateOfBirth: '1990-01-01',
            } as unknown as User),
            updateCurrentUserProfile: () => of(void 0),
        };

        await TestBed.configureTestingModule({
            imports: [EditUserComponent],
            providers: [{ provide: UserService, useValue: mockUserService }],
        }).compileComponents();
    });

    function create(): ComponentFixture<EditUserComponent> {
        const f = TestBed.createComponent(EditUserComponent);
        return f;
    }

    it('shows loading overlay while profile request pending and fills form after', async () => {
        const subject = new Subject<User>();
        mockUserService.getCurrentUserProfile = () => subject.asObservable();

        fixture = create();
        fixture.componentRef.setInput('display', true);
        fixture.detectChanges();

        // loading overlay should be present
        expect(fixture.nativeElement.textContent).toContain('autorenew');

        // emit profile
        subject.next({
            id: '940fad85-d3a9-4e11-a4c5-1cf268bf0b99',
            userName: 'user2',
            emailAddress: 'user2@example.com',
            firstName: 'First2',
            lastName: 'Last2',
            dateOfBirth: '1221-12-12',
        } as unknown as User);
        subject.complete();
        fixture.detectChanges();
        await fixture.whenStable();

        // overlay gone and form populated
        expect(fixture.nativeElement.querySelector('input[name="firstName"]').value).toBe('First2');
        expect(fixture.nativeElement.querySelector('input[name="lastName"]').value).toBe('Last2');
        expect(fixture.nativeElement.querySelector('input[name="dateOfBirth"]').value).toBe('1221-12-12');
    });

    it('disables Save when validation fails and enables when valid', async () => {
        fixture = create();
        fixture.componentRef.setInput('display', true);
        fixture.detectChanges();
        await fixture.whenStable();

        const first = fixture.nativeElement.querySelector('input[name="firstName"]');
        const last = fixture.nativeElement.querySelector('input[name="lastName"]');
        const dob = fixture.nativeElement.querySelector('input[name="dateOfBirth"]');

        // set invalid values
        first.value = 'A'; first.dispatchEvent(new Event('input'));
        last.value = 'B'; last.dispatchEvent(new Event('input'));
        dob.value = '3000-01-01'; dob.dispatchEvent(new Event('input'));
        fixture.detectChanges();

        const saveBtn = fixture.nativeElement.querySelector('button[type="submit"]');
        expect(saveBtn.disabled).toBe(true);

        // set valid
        first.value = 'Anna'; first.dispatchEvent(new Event('input'));
        last.value = 'Nowak'; last.dispatchEvent(new Event('input'));
        dob.value = '1990-01-01'; dob.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        await fixture.whenStable();

        expect(saveBtn.disabled).toBe(false);
    });

    it('calls updateCurrentUserProfile on submit', async () => {
        const spyUpdate = vi.fn().mockReturnValue(of(void 0));
        mockUserService.updateCurrentUserProfile = spyUpdate as any;

        fixture = create();
        fixture.componentRef.setInput('display', true);
        fixture.detectChanges();
        await fixture.whenStable();

        const first = fixture.nativeElement.querySelector('input[name="firstName"]');
        const last = fixture.nativeElement.querySelector('input[name="lastName"]');
        const dob = fixture.nativeElement.querySelector('input[name="dateOfBirth"]');

        first.value = 'Anna'; first.dispatchEvent(new Event('input'));
        last.value = 'Nowak'; last.dispatchEvent(new Event('input'));
        dob.value = '1990-01-01'; dob.dispatchEvent(new Event('input'));
        fixture.detectChanges();

        const form = fixture.nativeElement.querySelector('form');
        form.dispatchEvent(new Event('submit'));
        fixture.detectChanges();
        await fixture.whenStable();

        expect(spyUpdate).toHaveBeenCalledWith({ firstName: 'Anna', lastName: 'Nowak', dateOfBirth: '1990-01-01' });
    });
});
