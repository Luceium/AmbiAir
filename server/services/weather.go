// weather.go
package services

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/neelp03/matter-controller/utils"
)

// ---------------------------------------------------------------------------
// Weather (temperature & rain)
// ---------------------------------------------------------------------------

// WeatherResponse mirrors the JSON returned by the standard forecast endpoint.
type WeatherResponse struct {
	Current struct {
		Temperature float64 `json:"temperature_2m"`
		Rain        float64 `json:"rain"`
	} `json:"current"`
}

func FetchOutdoorWeather() (*WeatherResponse, error) {
	url := "https://api.open-meteo.com/v1/forecast?latitude=37.3387&longitude=-121.8853&current=temperature_2m,rain"
	resp, err := http.Get(url)
	if err != nil {
		return nil, fmt.Errorf("failed to call Open‑Meteo API: %w", err)
	}
	defer resp.Body.Close()

	var weather WeatherResponse
	if err := json.NewDecoder(resp.Body).Decode(&weather); err != nil {
		return nil, fmt.Errorf("failed to decode Open‑Meteo response: %w", err)
	}
	return &weather, nil
}

func FetchOutdoorTemperature() (float64, error) {
	weather, err := FetchOutdoorWeather()
	if err != nil {
		return 0, err
	}
	return utils.CToF(weather.Current.Temperature), nil
}

func FetchOutdoorRain() (float64, error) {
	weather, err := FetchOutdoorWeather()
	if err != nil {
		return 0, err
	}
	return weather.Current.Rain, nil
}

type aqiResponse struct {
	Current struct {
		AQI float64 `json:"us_aqi"`
	} `json:"current"`
}

// FetchOutdoorAQI fetches the latest PM2.5 & PM10 readings for today and
// returns the higher of their calculated US AQI values.
func FetchOutdoorAQI() (float64, error) {
	url := "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=37.3387&longitude=-121.8853&current=us_aqi"

	resp, err := http.Get(url)
	if err != nil {
		return 0, fmt.Errorf("failed to call Open‑Meteo AQI API: %w", err)
	}
	defer resp.Body.Close()

	var data aqiResponse
	if err := json.NewDecoder(resp.Body).Decode(&data); err != nil {
		return 0, fmt.Errorf("failed to decode AQI response: %w", err)
	}
	return data.Current.AQI, nil
}
