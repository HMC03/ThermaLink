#include "esp_log.h"
#include "mqtt_client.h"
#include "config.h"
#include "hivemq.h"

static const char *TAG = "HIVEMQ";

extern const uint8_t hivemq_crt_start[] asm("_binary_hivemq_crt_start");
extern const uint8_t hivemq_crt_end[] asm("_binary_hivemq_crt_end");

static bool mqtt_connected = false;
static mqtt_message_callback_t s_callback = NULL;

static void mqtt_event_handler(void *handler_args,
                               esp_event_base_t base,
                               int32_t event_id,
                               void *event_data)
{
    esp_mqtt_event_handle_t event = event_data;

    switch ((esp_mqtt_event_id_t)event_id) {

    case MQTT_EVENT_CONNECTED:
        // ESP_LOGI(TAG, "MQTT Connected");
        mqtt_connected = true;
        if (s_callback) s_callback(MQTT_EVT_CONNECTED, NULL, NULL, 0, 0);
        break;

    case MQTT_EVENT_DISCONNECTED:
        // ESP_LOGW(TAG, "MQTT Disconnected");
        mqtt_connected = false;
        if (s_callback) s_callback(MQTT_EVT_DISCONNECTED, NULL, NULL, 0, 0);
        break;

    case MQTT_EVENT_SUBSCRIBED:
        // ESP_LOGI(TAG, "Subscribed, msg_id=%d", event->msg_id);
        if (s_callback) s_callback(MQTT_EVT_SUBSCRIBED, NULL, NULL, 0, event->msg_id);
        break;

    case MQTT_EVENT_PUBLISHED:
        // ESP_LOGI(TAG, "Published, msg_id=%d", event->msg_id);
        if (s_callback) s_callback(MQTT_EVT_PUBLISHED, NULL, NULL, 0, event->msg_id);
        break;

    case MQTT_EVENT_DATA:
        // ESP_LOGI(TAG, "[MESSAGE] Topic=%.*s, Data=%.*s",
        //          event->topic_len, event->topic,
        //          event->data_len, event->data);
        if (s_callback) s_callback(MQTT_EVT_MESSAGE, event->topic, event->data, event->data_len, event->msg_id);
        break;

    default:
        break;
    }
}

esp_mqtt_client_handle_t hivemq_init_client(const char* lastwill_topic, mqtt_message_callback_t callback)
{
    static esp_mqtt_client_handle_t s_client = NULL;
    if (s_client) return s_client;

    esp_mqtt_client_config_t mqtt_cfg = {
        .broker = {
            .address.uri = MQTT_BROKER_URI,
            .verification.certificate = (const char *)hivemq_crt_start,
        },
        .credentials = {
            .username = MQTT_USERNAME,
            .authentication.password = MQTT_PASSWORD,
        },
        .session = {
            .last_will = {
                .topic = lastwill_topic,
                .msg   = "offline",
                .msg_len = 7,
                .qos   = 1,
                .retain = true
            },
            .disable_clean_session = false,
            .keepalive = 10
        }
    };

    s_callback = callback;
    s_client = esp_mqtt_client_init(&mqtt_cfg);
    esp_mqtt_client_register_event(
        s_client,
        ESP_EVENT_ANY_ID,
        mqtt_event_handler,
        NULL
    );

    return s_client;
}

bool hivemq_is_connected(void) {
    return mqtt_connected;
}
