package services

import (
	"bytes"
	"fmt"
	"os/exec"
	"strconv"
	"time"
)

const (
	deltaThreshold = 5.0
	targetComfort  = 70.0
	// These thresholds are arbitrary and should be adjusted based on user preferences.
	rainThreshold = 10.0
	aqiThreshold  = 100.0
	tick          = time.Minute
)

/*
Rule based check responsible for deciding if the window can be opened
based on the following inputs:
1. Rain rate (mm) from Open-Meteo Weather API
2. Outdoor air-quality index (US AQI) from Open-Meteo Air-Quality API
*/
func canWindowBeOpen() bool {
	rain, err := FetchOutdoorRain()
	if err != nil {
		fmt.Println("Error fetching outdoor rain:", err)
		return false
	}

	aqi, err := FetchOutdoorAQI()
	if err != nil {
		fmt.Println("Error fetching outdoor AQI:", err)
		return false
	}

	return rain <= rainThreshold && aqi <= aqiThreshold
}

/*
ruleBasedController is the rule‑based controller responsible for deciding when
to open or close the window.

Inputs (read every 1 minute):
  - Indoor temperature (°F) from the indoor sensor
  - Outdoor temperature (°F) from the Open-Meteo Weather API
  - Forecast in the future

Decision logic:

  - If the indoor temperature is outside the comfort range (65°F to 75°F) and the
    outdoor temperature is in the direction of the comfort range then open the window.
    ex. Indoor: 50°F, Outdoor: 100°F both are outside the comfort range but
    the outdoor temperature is in the direction of the comfort range and will bring
    the indoor range toward the comfort range.

    Returns:

  - triggerEvent: true if a window command should be sent, false if it should not

  - shouldOpen: true if the window should be opened, false if it should be closed

  - If triggerEvent is false, shouldOpen is ignored.
*/
func ruleBasedControllerEval(indoor float64, outdoor float64) (bool, bool) {
	comfortNeeds := indoor < (targetComfort-deltaThreshold) || indoor > (targetComfort+deltaThreshold)
	if !comfortNeeds {
		return false, false
	}

	if (indoor < (targetComfort-deltaThreshold) && outdoor > indoor) || (indoor > (targetComfort+deltaThreshold) && outdoor < indoor) {
		return !WindowOpen, true
	} else {
		return WindowOpen, false
	}
}

func ModelBasedControllerEval(indoor float64, outdoor float64) (bool, bool) {
	seconds := time.Now().Hour()*3600 + time.Now().Minute()*60 + time.Now().Second()
	args := []string{
		"inference.py",
		fmt.Sprintf("%.2f", indoor),
		fmt.Sprintf("%.2f", outdoor),
		strconv.Itoa(seconds),
		strconv.FormatBool(WindowOpen),
	}

	cmd := exec.Command("python3", args...)
	cmd.Dir = "ml_model"
	output, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Println("Error running model inference:", err)
		fmt.Println("Output:", string(output))
		return false, false
	}

	result := string(bytes.TrimSpace(output))
	if result == "True" {
		return true, !WindowOpen
	}
	return false, false
}


/*
Once a minute will check if the window can be open.
If it can't and the window is open, close it.
If it can the system will evaluate if a window event should be triggered.
*/
func automatedControllerEval() {
	WindowMu.Lock()
	defer WindowMu.Unlock()
	// Check if the window can be open
	canBeOpen := canWindowBeOpen()

	if !canBeOpen {
		if WindowOpen {
			err := CloseWindow()
			if err != nil {
				fmt.Println("Error closing window:", err)
			} else {
				fmt.Println("Window closed")
			}
		}
		return
	}

	indoor, outdoor := getTemps()

	// Evaluate if a window event should be triggered
	// modelBasedControllerEval()
	trigger, shouldOpen := ModelBasedControllerEval(indoor, outdoor)

	if !trigger {
		return
	}

	if shouldOpen {
		err := OpenWindow()
		if err != nil {
			fmt.Println("Error opening window:", err)
		} else {
			fmt.Println("Window opened")
		}
	} else {
		err := CloseWindow()
		if err != nil {
			fmt.Println("Error closing window:", err)
		} else {
			fmt.Println("Window closed")
		}
	}
}

func RunAutomatedController() {
	// Run the automated controller every minute
	ticker := time.NewTicker(time.Minute)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			automatedControllerEval()
		}
	}
}

// Planned for deprecation. Migrate to a new shared temperature data service.
func getTemps() (float64, float64) {
	indoorTemp, err := ReadTemperature()
	if err != nil {
		fmt.Println("Error reading indoor temperature:", err)
		return 0, 0
	}

	outdoorTemp, err := FetchOutdoorTemperature()
	if err != nil {
		fmt.Println("Error fetching outdoor temperature:", err)
		return 0, 0
	}

	return indoorTemp, outdoorTemp
}
