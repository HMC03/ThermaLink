#include "relay.h"
#include "driver/gpio.h"
#include "esp_log.h"
#include "mqtt_client.h"

#define RELAY_HEATER_PIN GPIO_NUM_26
#define RELAY_FAN_PIN    GPIO_NUM_25

static const char *TAG = "RELAY";

// Internal states
static bool heater_on = false;
static bool fan_on    = false;

// 👇 comes from main.c
extern esp_mqtt_client_handle_t s_client;

void relay_init(void)
{
    gpio_reset_pin(RELAY_HEATER_PIN);
    gpio_set_direction(RELAY_HEATER_PIN, GPIO_MODE_OUTPUT);

    gpio_reset_pin(RELAY_FAN_PIN);
    gpio_set_direction(RELAY_FAN_PIN, GPIO_MODE_OUTPUT);

    gpio_set_level(RELAY_HEATER_PIN, 0);
    gpio_set_level(RELAY_FAN_PIN, 0);

    heater_on = false;
    fan_on    = false;
}

void relay_set_heater(bool on)
{
    if (heater_on == on) return;

    heater_on = on;
    gpio_set_level(RELAY_HEATER_PIN, on ? 1 : 0);

    if (s_client) {
        esp_mqtt_client_publish(
            s_client,
            "roomA/heater/status",
            on ? "on" : "off",
            0, 1, 1
        );
    }

    ESP_LOGI(TAG, "Heater → %s", on ? "ON" : "OFF");
}

void relay_set_fan(bool on)
{
    if (fan_on == on) return;  // no change

    fan_on = on;
    gpio_set_level(RELAY_FAN_PIN, on ? 1 : 0);

    if (s_client) {
        esp_mqtt_client_publish(
            s_client,
            "roomA/fan/status",
            on ? "on" : "off",
            0, 1, 1
        );
    }

    ESP_LOGI(TAG, "Fan → %s", on ? "ON" : "OFF");
}

void relay_update(float current_temp, float target_temp, bool person_present)
{
    ESP_LOGI(TAG,
        "Relay logic: Tcur=%.1f Ttarget=%.1f person=%d",
        current_temp, target_temp, person_present);

    if (!person_present) {
        relay_set_fan(false);
        relay_set_heater(false);
        return;
    }

    // Too cold → turn both on
    if (current_temp < target_temp) {
        relay_set_heater(true);
        relay_set_fan(true);
        return;
    }

    // Too hot → heater off only fan
    if (current_temp > target_temp) {
        relay_set_heater(false);
        relay_set_fan(true);
        return;
    }

    // Exactly at target → turn off both
    relay_set_fan(false);
    relay_set_heater(false);
}

bool relay_get_heater(void)
{
    return heater_on;
}

bool relay_get_fan(void)
{
    return fan_on;
}