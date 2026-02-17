#include "Matter.h"
#include <app/server/OnboardingCodesUtil.h>
#include <credentials/examples/DeviceAttestationCredsExample.h>
#include <Stepper.h>

using namespace chip;
using namespace chip::app::Clusters;
using namespace esp_matter;
using namespace esp_matter::endpoint;

/* ------------ stepper setup ------------ */
constexpr int stepsPerRevolution = 2048;   // 28BYJ‑48
constexpr int windowTravelSteps  = 512;    // tune this
#define IN1 19
#define IN2 18
#define IN3 5
#define IN4 17
Stepper myStepper(stepsPerRevolution, IN1, IN3, IN2, IN4);

/* ------------ Matter globals ------------ */
const uint32_t CLUSTER_ID   = OnOff::Id;
const uint32_t ATTRIBUTE_ID = OnOff::Attributes::OnOff::Id;
uint16_t   light_endpoint_id = 0;
attribute_t *attribute_ref   = nullptr;

/* ------------ state & helpers ------------ */
bool windowOpen = false;

void doSteps(int32_t steps)
{
    xTaskCreatePinnedToCore(
        [](void *arg)
        {
            int32_t s = reinterpret_cast<intptr_t>(arg);
            myStepper.step(s);
            vTaskDelete(nullptr);
        },
        "stepper_task",
        4096,
        (void*)steps,
        1,
        nullptr,
        1);
}

void open_window()
{
    if (windowOpen) return;
    doSteps(+windowTravelSteps);
    windowOpen = true;
}

void close_window()
{
    if (!windowOpen) return;
    doSteps(-windowTravelSteps);
    windowOpen = false;
}

/* ------------ callbacks ------------ */
static esp_err_t on_attribute_update(attribute::callback_type_t type,
                                     uint16_t endpoint_id,
                                     uint32_t cluster_id,
                                     uint32_t attribute_id,
                                     esp_matter_attr_val_t *val,
                                     void *priv_data)
{
    if (type == attribute::PRE_UPDATE &&
        endpoint_id == light_endpoint_id &&
        cluster_id  == CLUSTER_ID &&
        attribute_id == ATTRIBUTE_ID)
    {
        bool new_state = val->val.b;
        Serial.printf("Window command: %s\n", new_state ? "open" : "close");
        new_state ? open_window() : close_window();
    }
    return ESP_OK;
}

static void on_device_event(const ChipDeviceEvent *, intptr_t) {}
static esp_err_t on_identification(identification::callback_type_t, uint16_t,
                                   uint8_t, uint8_t, void *) { return ESP_OK; }

/* ------------ setup / loop ------------ */
void setup()
{
    Serial.begin(115200);
    myStepper.setSpeed(5);

    esp_matter::factory_reset();
    esp_log_level_set("*", ESP_LOG_INFO);

    node::config_t node_config;
    node_t *node = node::create(&node_config, on_attribute_update, on_identification);

    on_off_light::config_t light_config;
    light_config.on_off.on_off = false;

    endpoint_t *endpoint = on_off_light::create(node, &light_config, ENDPOINT_FLAG_NONE, nullptr);
    attribute_ref     = attribute::get(cluster::get(endpoint, CLUSTER_ID), ATTRIBUTE_ID);
    light_endpoint_id = endpoint::get_id(endpoint);

    esp_matter::set_custom_dac_provider(Credentials::Examples::GetExampleDACProvider());
    esp_matter::start(on_device_event);

    PrintOnboardingCodes(chip::RendezvousInformationFlags(chip::RendezvousInformationFlag::kBLE));
    Serial.println("Matter window‑motor devi    ce ready");
}

void loop() { /* Matter runs in background */ }
