// main.go
package main

import (
	"fmt"
	"net/http"
	"os"

	"github.com/neelp03/matter-controller/handlers"
	"github.com/neelp03/matter-controller/services"
	"github.com/neelp03/matter-controller/utils"
)

func main() {
	fmt.Println("========== Starting Matter controller server ==========")

	utils.LoadEnv()
	ssid := os.Getenv("WIFI_SSID")
	password := os.Getenv("WIFI_PASSWORD")
	if ssid == "" || password == "" {
		fmt.Println("!!!!!!!!!! WIFI_SSID and WIFI_PASSWORD must be set in the environment !!!!!!!!!!")
		return
	}

	// Check and pair Temperature Sensor (endpoint 1)
	if services.IsDeviceCommissioned(1) {
		fmt.Println("++++++++++ Temperature sensor is already commissioned ++++++++++")
	} else {
		if err := services.PairDeviceOverBLE(1, ssid, password); err != nil {
			fmt.Println("!!!!!!!!!! Failed to pair temperature sensor !!!!!!!!!!:", err)
			return
		}
		fmt.Println("++++++++++ Paired temperature sensor ++++++++++")
	}

	// Check and pair Window Motor (endpoint 2)
	if services.IsDeviceCommissioned(3) {
		fmt.Println("++++++++++ Window motor is already commissioned ++++++++++")
	} else {
		if err := services.PairDeviceOverBLE(3, ssid, password); err != nil {
			fmt.Println("!!!!!!!!!! Failed to pair window motor !!!!!!!!!!:", err)
			return
		}
		fmt.Println("++++++++++ Paired window motor ++++++++++")
	}

	// Start periodic data backup
	backupInterval := 60 // seconds
	go services.StartDBServices(backupInterval)
	fmt.Println("========== Periodic data backup started every", backupInterval, "seconds ==========")

	// // Launch the rule‑based controller
	// go services.RunAutomatedController()
	// fmt.Println("========== Rule‑based climate control loop started ==========")

	http.HandleFunc("/temperature", handlers.TemperatureHandler)
	http.HandleFunc("/weather", handlers.WeatherHandler)
	http.HandleFunc("/window", handlers.WindowStatusHandler)
	http.HandleFunc("/toggle-window", handlers.ToggleWindowHandler)

	fmt.Println("========== Server is now listening on port 8080 ==========")
	err := http.ListenAndServe("0.0.0.0:8080", nil)
	if err != nil {
		fmt.Println("!!!!!!!!!! Failed to start server !!!!!!!!!!:", err)
	}
}
