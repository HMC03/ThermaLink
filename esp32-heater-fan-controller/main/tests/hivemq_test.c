#include "esp_log.h"
#include "wifi.h"
#include "hivemq.h"
#include "mqtt_client.h"

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

static const char *TAG = "HIVEMQ_TEST";

void mqtt_handler(mqtt_event_type_t type, const char* topic, const char* payload, int len, int msg_id)
{
    switch(type) {
        case MQTT_EVT_CONNECTED:
            ESP_LOGI(TAG, "MQTT connected!");
            break;
        case MQTT_EVT_DISCONNECTED:
            ESP_LOGW(TAG, "MQTT disconnected!");
            break;
        case MQTT_EVT_SUBSCRIBED:
            ESP_LOGI(TAG, "Subscribed, msg_id=%d", msg_id);
            break;
        case MQTT_EVT_PUBLISHED:
            ESP_LOGI(TAG, "Published, msg_id=%d", msg_id);
            break;
        case MQTT_EVT_MESSAGE:
            ESP_LOGI(TAG, "Message on %s: %.*s", topic, len, payload);
            break;
    }
}

void app_main(void)
{
    ESP_LOGI(TAG, "Hivemq_test starting...");

    // 1) Bring up WiFi
    ESP_ERROR_CHECK(wifi_connect());

    // 2) Connect MQTT
    esp_mqtt_client_handle_t s_client = NULL;
    s_client = hivemq_init_client("hivemq_test/status", mqtt_handler);
    esp_mqtt_client_start(s_client);

    while(!hivemq_is_connected()) {
        ESP_LOGI(TAG, "Waiting for MQTT connection...");
        vTaskDelay(pdMS_TO_TICKS(1000));
    }

    // 3)  Publish online status
    esp_mqtt_client_publish(s_client, "hivemq_test/status", "online", 0, 1, 1);

    // 3)  Subscribe to topic for commands
    esp_mqtt_client_subscribe(s_client, "hivemq_test/heartbeat", 2);
    
    // 4) Loop — send heartbeat
    for(int i = 0; i < 5; i++) {
        char buf[16];
        snprintf(buf, sizeof(buf), "%d", i);
        esp_mqtt_client_publish(s_client, "hivemq_test/heartbeat", buf, 0, 2, 0);
        vTaskDelay(pdMS_TO_TICKS(5000));
    }

    // Cleanup
    esp_mqtt_client_publish(s_client, "hivemq_test/status", "offline", 0, 1, 1);
    esp_mqtt_client_stop(s_client);
    ESP_LOGI(TAG, "Hivemq_test completed.");
}
