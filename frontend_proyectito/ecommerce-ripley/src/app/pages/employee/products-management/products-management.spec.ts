import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductsManagement } from './products-management';

describe('ProductsManagement', () => {
  let component: ProductsManagement;
  let fixture: ComponentFixture<ProductsManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductsManagement],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
