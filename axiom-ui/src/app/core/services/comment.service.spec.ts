import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CommentService } from './comment.service';

describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads comments for a work item', () => {
    service.getComments('work-1').subscribe((comments) => {
      expect(comments).toEqual([
        {
          id: 'comment-1',
          workItemId: 'work-1',
          authorId: 'user-1',
          author: 'Ada Lovelace',
          text: 'Looks good.',
          createdOn: '2026-01-01T12:00:00',
        },
      ]);
    });

    const request = httpMock.expectOne('/api/work-items/work-1/comments');
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 'comment-1',
        workItemId: 'work-1',
        authorId: 'user-1',
        author: 'Ada Lovelace',
        text: 'Looks good.',
        createdOn: '2026-01-01T12:00:00',
      },
    ]);
  });

  it('posts new comment text', () => {
    service.addComment('work-1', 'Ship it.').subscribe((comment) => {
      expect(comment.text).toBe('Ship it.');
    });

    const request = httpMock.expectOne('/api/work-items/work-1/comments');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ text: 'Ship it.' });
    request.flush({
      id: 'comment-1',
      workItemId: 'work-1',
      authorId: 'user-1',
      author: 'Ada Lovelace',
      text: 'Ship it.',
      createdOn: '2026-01-01T12:00:00',
    });
  });

  it('deletes comments by id', () => {
    service.deleteComment('comment-1').subscribe((result) => {
      expect(result).toBeNull();
    });

    const request = httpMock.expectOne('/api/comments/comment-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
