#pragma once
#include "mqtt_client.h"
#include <stdbool.h>

typedef enum {
    MQTT_EVT_CONNECTED,
    MQTT_EVT_DISCONNECTED,
    MQTT_EVT_SUBSCRIBED,
    MQTT_EVT_PUBLISHED,
    MQTT_EVT_MESSAGE
} mqtt_event_type_t;

typedef void (*mqtt_message_callback_t)(
    mqtt_event_type_t type,
    const char* topic,
    const char* payload,
    int len,
    int msg_id
);

// Initialize the MQTT client with last will topic and callback
esp_mqtt_client_handle_t hivemq_init_client(
    const char* lastwill_topic,
    mqtt_message_callback_t callback
);

// Return true if MQTT is connected
bool hivemq_is_connected(void);
