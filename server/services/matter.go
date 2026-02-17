package services

import (
	"fmt"
	"github.com/neelp03/matter-controller/utils"
	"os/exec"
	"regexp"
	"strconv"
	"strings"
)

func IsDeviceCommissioned(endpoint int) bool {
	cmd := exec.Command("../connectedhomeip/out/host/chip-tool", "operationalcredentials", "read", "fabrics", fmt.Sprintf("%d", endpoint), "0")
	output, err := cmd.CombinedOutput()
	if err != nil {
		fmt.Println("!!!!!!!!!! Error checking commissioned status !!!!!!!!!!:", err)
		return false
	}

	outStr := string(output)
	if strings.Contains(outStr, "Fabrics: 0 entries") {
		return false
	}
	if strings.Contains(outStr, "Fabrics: 1 entries") {
		return true
	}
	return false
}

func PairDeviceOverBLE(endpoint int, ssid, password string) error {
	fmt.Printf("========== Device %d not commissioned. Attempting to pair via BLE... ==========\n", endpoint)
	cmd := exec.Command("../connectedhomeip/out/host/chip-tool", "pairing", "ble-wifi", fmt.Sprintf("%d", endpoint), ssid, password, "20202021", "3840")
	cmdOut, err := cmd.CombinedOutput()
	fmt.Println("========== Pairing output ==========")
	fmt.Println(string(cmdOut))
	if err != nil {
		return fmt.Errorf("!!!!!!!!!! Pairing failed !!!!!!!!!!: %v", err)
	}
	fmt.Println("++++++++++ Pairing succeeded ++++++++++")
	return nil
}

func ReadTemperature() (float64, error) {
	cmd := exec.Command("../connectedhomeip/out/host/chip-tool", "temperaturemeasurement", "read", "measured-value", "1", "1")
	output, err := cmd.CombinedOutput()
	if err != nil {
		return 0, fmt.Errorf("!!!!!!!!!! command failed !!!!!!!!!!: %v", err)
	}

	re := regexp.MustCompile(`(?m)MeasuredValue: (\d+)`)
	matches := re.FindStringSubmatch(string(output))
	if len(matches) < 2 {
		return 0, fmt.Errorf("!!!!!!!!!! could not parse temperature from output !!!!!!!!!!")
	}

	tempRaw, err := strconv.Atoi(matches[1])
	if err != nil {
		return 0, fmt.Errorf("!!!!!!!!!! invalid temperature format !!!!!!!!!!: %v", err)
	}

	celsius := float64(tempRaw) / 100.0
	fahrenheit := utils.CToF(celsius)

	return fahrenheit, nil
}

func OpenWindowMotor() error {
	fmt.Println("========== Sending Matter command: OPEN window motor ==========")
	cmd := exec.Command("../connectedhomeip/out/host/chip-tool", "onoff", "on", "3", "1")
	cmdOut, err := cmd.CombinedOutput()
	fmt.Println(string(cmdOut))
	if err != nil {
		return fmt.Errorf("!!!!!!!!!! Failed to open window motor !!!!!!!!!!: %v", err)
	}
	return nil
}

func CloseWindowMotor() error {
	fmt.Println("========== Sending Matter command: CLOSE window motor ==========")
	cmd := exec.Command("../connectedhomeip/out/host/chip-tool", "onoff", "off", "3", "1")
	cmdOut, err := cmd.CombinedOutput()
	fmt.Println(string(cmdOut))
	if err != nil {
		return fmt.Errorf("!!!!!!!!!! Failed to close window motor !!!!!!!!!!: %v", err)
	}
	return nil
}