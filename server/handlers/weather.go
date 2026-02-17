package handlers

import (
	"fmt"
	"net/http"
	"github.com/neelp03/matter-controller/services"
)

func WeatherHandler(w http.ResponseWriter, r *http.Request) {
	weather, err := services.FetchOutdoorWeather()
	if err != nil {
		fmt.Println("!!!!!!!!!! Failed to fetch weather !!!!!!!!!!:", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	response := fmt.Sprintf("Outdoor Temp: %.2f°C, Rain: %.2fmm", weather.Current.Temperature, weather.Current.Rain)

	fmt.Println("========== Responding with weather ==========")
	fmt.Println(response)
	w.Write([]byte(response))
}