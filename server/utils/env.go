package utils

import (
	"fmt"
	"os"

	"github.com/joho/godotenv"
)

func LoadEnv() {
	err := godotenv.Load()
	if err != nil {
		fmt.Println("!!!!!!!!!! Error loading .env file !!!!!!!!!!:", err)
	}
}

func GetEnv(key string) string {
	value := os.Getenv(key)
	if value == "" {
		fmt.Printf("!!!!!!!!!! %s not set in environment !!!!!!!!!!\n", key)
	}
	return value
}
