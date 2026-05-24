package utils

import (
	"testing"
	"time"
)

func TestWeekRange(t *testing.T) {
	tests := []struct {
		name     string
		input    time.Time
		wantStart time.Time
		wantEnd   time.Time
	}{
		{
			name:     "Monday",
			input:    time.Date(2026, 5, 25, 10, 30, 0, 0, time.Local), // 2026-05-25 is a Monday
			wantStart: time.Date(2026, 5, 25, 0, 0, 0, 0, time.Local),
			wantEnd:   time.Date(2026, 5, 31, 23, 59, 59, 999999999, time.Local),
		},
		{
			name:     "Sunday",
			input:    time.Date(2026, 5, 31, 23, 59, 59, 999999999, time.Local), // 2026-05-31 is a Sunday
			wantStart: time.Date(2026, 5, 25, 0, 0, 0, 0, time.Local),
			wantEnd:   time.Date(2026, 5, 31, 23, 59, 59, 999999999, time.Local),
		},
		{
			name:     "Midweek",
			input:    time.Date(2026, 5, 27, 12, 0, 0, 0, time.Local), // Wednesday
			wantStart: time.Date(2026, 5, 25, 0, 0, 0, 0, time.Local),
			wantEnd:   time.Date(2026, 5, 31, 23, 59, 59, 999999999, time.Local),
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			start, end := WeekRange(tt.input)
			if !start.Equal(tt.wantStart) {
				t.Errorf("start = %v, want %v", start, tt.wantStart)
			}
			if !end.Equal(tt.wantEnd) {
				t.Errorf("end = %v, want %v", end, tt.wantEnd)
			}
		})
	}
}