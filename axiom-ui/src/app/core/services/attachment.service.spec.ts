import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AttachmentService } from './attachment.service';

describe('AttachmentService', () => {
  let service: AttachmentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AttachmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads attachments for a work item', () => {
    service.getAttachments('work-1').subscribe((attachments) => {
      expect(attachments).toEqual([
        {
          id: 'attachment-1',
          workItemId: 'work-1',
          uploadedBy: 'user-1',
          fileName: 'brief.pdf',
          fileSize: 1024,
          fileType: 'application/pdf',
          uploadedOn: '2026-01-01T12:00:00',
        },
      ]);
    });

    const request = httpMock.expectOne('/api/work-items/work-1/attachments');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 'attachment-1',
        workItemId: 'work-1',
        uploadedBy: 'user-1',
        fileName: 'brief.pdf',
        fileSize: 1024,
        fileType: 'application/pdf',
        uploadedOn: '2026-01-01T12:00:00',
      },
    ]);
  });

  it('uploads attachments as multipart form data', () => {
    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' });

    service.uploadAttachment('work-1', file).subscribe((id) => {
      expect(id).toBe('attachment-1');
    });

    const request = httpMock.expectOne('/api/work-items/work-1/attachments');
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBe(true);
    expect(request.request.body.get('file')).toBe(file);
    request.flush('attachment-1');
  });

  it('downloads an attachment as a blob', () => {
    const blob = new Blob(['payload'], { type: 'text/plain' });

    service.downloadAttachment('attachment-1').subscribe((result) => {
      expect(result).toBe(blob);
    });

    const request = httpMock.expectOne('/api/attachments/attachment-1/download');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(blob);
  });

  it('deletes an attachment', () => {
    service.deleteAttachment('attachment-1').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/attachments/attachment-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
