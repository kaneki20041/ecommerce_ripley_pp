import { TestBed } from '@angular/core/testing';

import { AuthModal } from './auth-modal';

describe('AuthModal', () => {
  let service: AuthModal;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthModal);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
