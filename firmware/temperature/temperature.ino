#include <DHT.h>
#include "Matter.h"
#include <app/server/OnboardingCodesUtil.h>
#include <credentials/examples/DeviceAttestationCredsExample.h>

using namespace chip;
using namespace chip::app::Clusters;
using namespace esp_matter;
using namespace esp_matter::endpoint;

// === Pins ===
#define DHTPIN 21
#define DHTTYPE DHT11
#define RESET_BUTTON_PIN 0     // Boot button GPIO
#define PAIRING_LED_PIN 2      // Built-in LED on many ESP32s (can change to another pin)

DHT dht(DHTPIN, DHTTYPE);

// === Matter Cluster Info ===
const uint32_t TEMP_CLUSTER_ID = TemperatureMeasurement::Id;
const uint32_t TEMP_ATTR_ID = TemperatureMeasurement::Attributes::MeasuredValue::Id;

uint16_t temp_endpoint_id = 0;
attribute_t *temp_attr_ref = nullptr;

// === Matter Callbacks ===
static void on_device_event(const ChipDeviceEvent *event, intptr_t arg) {}
static esp_err_t on_identification(identification::callback_type_t type, uint16_t endpoint_id,
                                   uint8_t effect_id, uint8_t effect_variant, void *priv_data) {
  return ESP_OK;
}
static esp_err_t on_attribute_update(attribute::callback_type_t type, uint16_t endpoint_id,
                                     uint32_t cluster_id, uint32_t attribute_id,
                                     esp_matter_attr_val_t *val, void *priv_data) {
  return ESP_OK;
}

void setup() {
  Serial.begin(115200);
  delay(1000);  // Let serial monitor connect
  
  // === Pins Setup ===
  pinMode(RESET_BUTTON_PIN, INPUT_PULLUP);
  pinMode(PAIRING_LED_PIN, OUTPUT);
  digitalWrite(PAIRING_LED_PIN, LOW);  // Off by default

  // === Blink LED in pairing mode ===
  for (int i = 0; i < 10; i++) {
    digitalWrite(PAIRING_LED_PIN, HIGH);
    delay(200);
    digitalWrite(PAIRING_LED_PIN, LOW);
    delay(200);
  }

  // === Enable Debug Logs ===
  esp_log_level_set("*", ESP_LOG_DEBUG);

  // === Matter Node Setup ===
  node::config_t node_config;
  node_t *node = node::create(&node_config, on_attribute_update, on_identification);

  temperature_sensor::config_t temp_config;
  temp_config.temperature_measurement.measured_value = (int16_t)0;
  temp_config.temperature_measurement.min_measured_value = (int16_t)-400;
  temp_config.temperature_measurement.max_measured_value = (int16_t)1250;

  endpoint_t *endpoint = temperature_sensor::create(node, &temp_config, ENDPOINT_FLAG_NONE, nullptr);
  temp_attr_ref = attribute::get(cluster::get(endpoint, TEMP_CLUSTER_ID), TEMP_ATTR_ID);
  temp_endpoint_id = endpoint::get_id(endpoint);

  // === Start Matter ===
  esp_matter::set_custom_dac_provider(Credentials::Examples::GetExampleDACProvider());
  esp_matter::start(on_device_event);

  PrintOnboardingCodes(chip::RendezvousInformationFlags(chip::RendezvousInformationFlag::kBLE));

  // === Turn LED ON to indicate ready status ===
  digitalWrite(PAIRING_LED_PIN, HIGH);  // Steady ON once paired
  dht.begin();
  Serial.println("🌡️ Matter Temperature Sensor ready and reporting ✅");
}

void update_temperature(float tempC) {
  int16_t matter_temp = static_cast<int16_t>(tempC * 100);  // x100
  esp_matter_attr_val_t new_val = esp_matter_nullable_int(matter_temp);
  attribute::update(temp_endpoint_id, TEMP_CLUSTER_ID, TEMP_ATTR_ID, &new_val);
}

unsigned long last_read = 0;

void loop() {
  if (millis() - last_read > 5000) {
    float tempC = dht.readTemperature();
    if (!isnan(tempC)) {
      Serial.printf("📡 Reporting temperature: %.2f°C\n", tempC);
      update_temperature(tempC);
    } else {
      Serial.println("⚠️ DHT11 read failed!");
    }
    last_read = millis();
  }
}
