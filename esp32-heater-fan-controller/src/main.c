#include "driver/gpio.h"
#include "driver/uart.h"
#include "esp_log.h"
#include <string.h>
#include <stdio.h>

#define RELAY1_PIN GPIO_NUM_25
#define RELAY2_PIN GPIO_NUM_26
#define UART_NUM   UART_NUM_0
#define BUF_SIZE   128

static const char *TAG = "RELAY";

void app_main(void) {
    // === 1. Setup GPIOs ===
    gpio_reset_pin(RELAY1_PIN);
    gpio_set_direction(RELAY1_PIN, GPIO_MODE_OUTPUT);
    gpio_reset_pin(RELAY2_PIN);
    gpio_set_direction(RELAY2_PIN, GPIO_MODE_OUTPUT);

    // === 2. Setup UART (115200, 8N1) ===
    uart_config_t uart_config = {
        .baud_rate = 115200,
        .data_bits = UART_DATA_8_BITS,
        .parity    = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
    };
    uart_param_config(UART_NUM, &uart_config);
    uart_set_pin(UART_NUM, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
    uart_driver_install(UART_NUM, BUF_SIZE * 2, 0, 0, NULL, 0);

    // === 3. Silence boot spam ===
    esp_log_level_set("*", ESP_LOG_NONE);
    esp_log_level_set(TAG, ESP_LOG_INFO);

    // === 4. Main loop ===
    char line[32];
    int len = 0;
    uint8_t data[1];

    ESP_LOGI(TAG, "Relay Control Ready!");
    uart_write_bytes(UART_NUM, "Commands: r1on, r1off, r2on, r2off\r\n> ", 40);

    while (1) {
        // Read 1 byte at a time (non-blocking)
        int read = uart_read_bytes(UART_NUM, data, 1, 10 / portTICK_PERIOD_MS);
        if (read > 0) {
            char c = data[0];

            if (c == '\r' || c == '\n') {
                if (len > 0) {
                    line[len] = '\0';

                    // Convert to lowercase
                    for (int i = 0; line[i]; i++) {
                        if (line[i] >= 'A' && line[i] <= 'Z') line[i] += 32;
                    }

                    ESP_LOGI(TAG, "CMD: %s", line);

                    if (strcmp(line, "r1on") == 0) {
                        gpio_set_level(RELAY1_PIN, 1);
                        uart_write_bytes(UART_NUM, "RELAY1 ON\r\n> ", 13);
                    } else if (strcmp(line, "r1off") == 0) {
                        gpio_set_level(RELAY1_PIN, 0);
                        uart_write_bytes(UART_NUM, "RELAY1 OFF\r\n> ", 14);
                    } else if (strcmp(line, "r2on") == 0) {
                        gpio_set_level(RELAY2_PIN, 1);
                        uart_write_bytes(UART_NUM, "RELAY2 ON\r\n> ", 13);
                    } else if (strcmp(line, "r2off") == 0) {
                        gpio_set_level(RELAY2_PIN, 0);
                        uart_write_bytes(UART_NUM, "RELAY2 OFF\r\n> ", 14);
                    } else {
                        uart_write_bytes(UART_NUM, "Unknown. Use: r1on, r1off, r2on, r2off\r\n> ", 49);
                    }

                    len = 0;
                }
            } else if (len < 31) {
                line[len++] = c;
            }
        }
    }
}