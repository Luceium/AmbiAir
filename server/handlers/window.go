package handlers

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/neelp03/matter-controller/services"
)

func WindowStatusHandler(w http.ResponseWriter, r *http.Request) {
	response := map[string]bool{"window": services.WindowOpen}
	json.NewEncoder(w).Encode(response)
}

func ToggleWindowHandler(w http.ResponseWriter, r *http.Request) {
	if services.WindowOpen {
		services.CloseWindow()
	} else {
		services.OpenWindow()
	}
	statusStr := "closed"
	if services.WindowOpen {
		statusStr = "open"
	}
	fmt.Println("========== Toggled window. Now:", statusStr, "==========")
	response := map[string]string{"window": statusStr}
	json.NewEncoder(w).Encode(response)
}
