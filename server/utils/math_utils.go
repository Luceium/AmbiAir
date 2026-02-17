package utils

import "time"

func CToF(c float64) float64 {
	return (c * 9 / 5) + 32
}

func TimeToMinutesSinceYearStart(t time.Time) int {
	yearStart := time.Date(t.Year(), time.January, 1, 0, 0, 0, 0, t.Location())
	return int(t.Sub(yearStart).Minutes())
}
