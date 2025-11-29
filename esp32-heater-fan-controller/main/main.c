#include <stdio.h>
#include <string.h>
#include <stdbool.h>

#include "esp_log.h"
#include "esp_system.h"
#include "mqtt_client.h"

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#include "cJSON.h"

#include "wifi.h"
#include "hivemq.h"
#include "relay.h"

// =====================================================
static const char* TAG = "MAIN";

// Exported → used by relay.c
esp_mqtt_client_handle_t s_client = NULL;

// State variables
static bool person_present = false;
static float current_temp = 0;
static float target_temp  = 0;
// =====================================================


static void handle_logic()
{
    relay_update(current_temp, target_temp, person_present);
}


static void handle_person_payload(const char* data, int len)
{
    cJSON* root = cJSON_ParseWithLength(data, len);
    if (!root) return;

    cJSON* st = cJSON_GetObjectItem(root, "status");
    if (cJSON_IsBool(st))
        person_present = st->valueint;  // 0/1

    cJSON_Delete(root);

    ESP_LOGI(TAG, "Person = %d", person_present);
    handle_logic();
}


static void handle_temp_payload(const char* data, int len, const char* topic)
{
    cJSON* root = cJSON_ParseWithLength(data, len);
    if (!root) return;

    cJSON* tf = cJSON_GetObjectItem(root, "temp_f");
    if (cJSON_IsNumber(tf)) {
        if (strcmp(topic, "roomA/temperature/status") == 0) {
            current_temp = cJSON_GetObjectItem(root, "temp_f")->valuedouble;
            ESP_LOGI(TAG, "TempStatus = %.1f", current_temp);
        }
        else if (strcmp(topic, "roomA/temperature/target") == 0) {
            target_temp = cJSON_GetObjectItem(root, "temp_f")->valuedouble;
            ESP_LOGI(TAG, "TempTarget = %.1f", target_temp);
        }
    }  

    cJSON_Delete(root);

    handle_logic();
}


void mqtt_handler(mqtt_event_type_t type,
                  const char* topic,
                  const char* payload,
                  int len,
                  int msg_id)
{
    switch(type) {

        case MQTT_EVT_CONNECTED:
            ESP_LOGI(TAG, "MQTT connected");
            break;

        case MQTT_EVT_DISCONNECTED:
            ESP_LOGW(TAG, "MQTT disconnected");
            break;

        case MQTT_EVT_SUBSCRIBED:
            ESP_LOGI(TAG, "Subscribed: msg_id=%d", msg_id);
            break;

        case MQTT_EVT_PUBLISHED:
            ESP_LOGI(TAG, "Published: msg_id=%d", msg_id);
            break;

        case MQTT_EVT_MESSAGE:
            ESP_LOGI(TAG, "MSG: %.*s = %.*s",
                     strlen(topic), topic,
                     len, payload);

            if      (strcmp(topic, "roomA/person/status") == 0)
                handle_person_payload(payload, len);

            else if (strcmp(topic, "roomA/temperature/status") == 0)
                handle_temp_payload(payload, len, topic);

            else if (strcmp(topic, "roomA/temperature/target") == 0)
                handle_temp_payload(payload, len, topic);

            break;
    }
}

void app_main(void)
{
    ESP_LOGI(TAG, "System boot...");

    // 1. WiFi
    ESP_ERROR_CHECK(wifi_connect());

    // 2. Relays
    relay_init();

    // 3. MQTT Client
    s_client = hivemq_init_client("roomA/device/status", mqtt_handler);
    esp_mqtt_client_start(s_client);

    while(!hivemq_is_connected()) {
        ESP_LOGI(TAG, "Waiting MQTT..");
        vTaskDelay(pdMS_TO_TICKS(500));
    }

    // 4. Announce online
    esp_mqtt_client_publish(
        s_client,
        "roomA/device/status",
        "online", 0, 1, 1
    );

    // 5. Subscribe to topics
    esp_mqtt_client_subscribe(s_client, "roomA/person/status", 1);
    esp_mqtt_client_subscribe(s_client, "roomA/temperature/status", 1);
    esp_mqtt_client_subscribe(s_client, "roomA/temperature/target", 1);

    ESP_LOGI(TAG, "Ready.");


    // 6. Main loop (everything else is event-driven)
    while (1) {
        vTaskDelay(pdMS_TO_TICKS(1000));
    }


    // 7. Cleanup (never reached)
    esp_mqtt_client_publish(
        s_client,
        "roomA/device/status",
        "offline", 0, 1, 1
    );
    esp_mqtt_client_stop(s_client);
}
