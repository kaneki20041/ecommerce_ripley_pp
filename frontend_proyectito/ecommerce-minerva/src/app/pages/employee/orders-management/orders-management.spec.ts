import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrdersManagement } from './orders-management';

describe('OrdersManagement', () => {
  let component: OrdersManagement;
  let fixture: ComponentFixture<OrdersManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersManagement],
    }).compileComponents();

    fixture = TestBed.createComponent(OrdersManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
