package services

import (
	"fmt"
	"sync"
)

var WindowOpen = false
var WindowEventFlag = false
var WindowMu sync.Mutex

func OpenWindow() error {
	WindowMu.Lock()
	defer WindowMu.Unlock()

	if WindowOpen {
		fmt.Println("Window is already open")
		return nil
	}

	err := OpenWindowMotor()
	if err == nil {
		fmt.Println("Opening window...")
		WindowOpen = true
		WindowEventFlag = !WindowEventFlag
	} else {
		fmt.Println("Error opening window:", err)
	}

	return err
}

func CloseWindow() error {
	WindowMu.Lock()
	defer WindowMu.Unlock()

	if !WindowOpen {
		fmt.Println("Window is already closed")
		return nil
	}

	err := CloseWindowMotor()
	if err == nil {
		fmt.Println("Closing window...")
		WindowOpen = false
		WindowEventFlag = !WindowEventFlag
	} else {
		fmt.Println("Error closing window:", err)
	}

	return nil
}
