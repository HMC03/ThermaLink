#include "esp_log.h"
#include "wifi.h"

static const char* TAG = "wifi_test";

void app_main(void)
{
    ESP_LOGI(TAG, "Wifi Test Starting...");

    // Call Wi-Fi function
    esp_err_t ret = wifi_connect();
    if (ret != ESP_OK) {
        ESP_LOGE(TAG, "Wi-Fi failed: %s", esp_err_to_name(ret));
        return;
    }

    ESP_LOGI(TAG, "Wi-Fi connected! System ready.");
}