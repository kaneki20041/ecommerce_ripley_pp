import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface StatsTrend {
  value: number;
  isUp: boolean;
}

@Component({
  selector: 'app-stats-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-card.html',
  styleUrls: ['./stats-card.scss']
})
export class StatsCardComponent {
  @Input() title: string = '';
  @Input() value: string | number = 0;
  @Input() icon: string = 'default';
  @Input() color: string = '#A8D8EA';
  @Input() trend?: StatsTrend;
  @Input() loading: boolean = false;
}