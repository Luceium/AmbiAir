package main

import (
	"fmt"
	"github.com/neelp03/matter-controller/services"
)

func main() {
	indoor := 72.0
	outdoor := 65.0

	trigger, shouldOpen := services.ModelBasedControllerEval(indoor, outdoor)

	fmt.Println("Trigger:", trigger)
	fmt.Println("Should Open:", shouldOpen)
}
