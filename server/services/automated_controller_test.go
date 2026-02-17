package services

import "testing"

/*
Rule Based Controller Tests grid
Rows: Indoor Temperature
Columns: Outdoor Temperature

________|Colder |Cold   |Neutral|Hot    |Hotter |
|Colder |       |Open   |       |       |       |
|Cold   |Close  |       |Open   |Open   |       |
|Neutral|       |Close  |NIL    |Close  |       |
|Hot    |       |Open   |Open   |       |Close  |
|Hotter |       |       |       |Open   |       |

Test name convention: TestFunction_IndoorTemp_OutdoorTemp
*/

const (
	Colder  = 40.0
	Cold    = 50.0
	Neutral = 70.0
	Hot     = 80.0
	Hotter  = 90.0
)

func TestRuleBasedControllerEval_Colder_Cold(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Colder, Cold)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Cold_Colder(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Cold, Colder)
	if shouldOpen != false {
		t.Errorf("Expected shouldOpen: false, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Cold_Neutral(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Cold, Neutral)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Cold_Hot(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Cold, Hot)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Neutral_Cold(t *testing.T) {
	trigger, shouldOpen := ruleBasedControllerEval(Neutral, Cold)
	if trigger != false || shouldOpen != false {
		t.Errorf("Expected trigger: false, shouldOpen: false, got trigger: %v, shouldOpen: %v", trigger, shouldOpen)
	}
}

func TestRuleBasedControllerEval_Neutral_Neutral(t *testing.T) {
	trigger, _ := ruleBasedControllerEval(Neutral, Cold)
	if trigger != false {
		t.Errorf("Expected trigger: false, got trigger: %v", trigger)
	}
}

func TestRuleBasedControllerEval_Neutral_Hot(t *testing.T) {
	trigger, shouldOpen := ruleBasedControllerEval(Neutral, Cold)
	if trigger != false || shouldOpen != false {
		t.Errorf("Expected trigger: false, shouldOpen: false, got trigger: %v, shouldOpen: %v", trigger, shouldOpen)
	}
}

func TestRuleBasedControllerEval_Hot_Cold(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Hot, Cold)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Hot_Neutral(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Hot, Neutral)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Hot_Hotter(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Hot, Hotter)
	if shouldOpen != false {
		t.Errorf("Expected shouldOpen: false, got shouldOpen: %v", shouldOpen)
	}
}

func TestRuleBasedControllerEval_Hotter_Hot(t *testing.T) {
	_, shouldOpen := ruleBasedControllerEval(Hotter, Hot)
	if shouldOpen != true {
		t.Errorf("Expected shouldOpen: true, got shouldOpen: %v", shouldOpen)
	}
}
