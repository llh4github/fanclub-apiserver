package utils

import "time"

// WeekRange 返回当前日期所属的周范围（周一 ~ 周日）
//
// Parameters:
//   - t: 时间点
//
// Returns:
//   - startOfWeek: 周一 00:00:00
//   - endOfWeek: 周日 23:59:59.999999999
func WeekRange(t time.Time) (startOfWeek, endOfWeek time.Time) {
	// Go's Weekday(): Sunday=0, Monday=1, ..., Saturday=6
	// Monday-based offset: Monday→0, Tuesday→1, ..., Sunday→6
	offset := (int(t.Weekday()) + 6) % 7

	startOfWeek = t.AddDate(0, 0, -offset)
	startOfWeek = time.Date(startOfWeek.Year(), startOfWeek.Month(), startOfWeek.Day(), 0, 0, 0, 0, startOfWeek.Location())

	endOfWeek = startOfWeek.AddDate(0, 0, 6)
	endOfWeek = time.Date(endOfWeek.Year(), endOfWeek.Month(), endOfWeek.Day(), 23, 59, 59, 999999999, endOfWeek.Location())

	return
}
