package handlers

import (
	"fmt"
	"net/http"
	"github.com/neelp03/matter-controller/services"
)

func TemperatureHandler(w http.ResponseWriter, r *http.Request) {
	temp, err := services.ReadTemperature()
	if err != nil {
		fmt.Println("!!!!!!!!!! Failed to read temperature !!!!!!!!!!:", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	response := fmt.Sprintf("Temperature: %.2f°F", temp)
	fmt.Println("========== Responding with temperature ==========")
	fmt.Println(response)
	w.Write([]byte(response))
}
