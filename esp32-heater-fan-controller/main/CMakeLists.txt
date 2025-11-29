idf_component_register(
    SRCS
    # "main.c"
    "wifi.c"
    "hivemq.c"
    # "tests/wifi_test.c"
    "tests/hivemq_test.c"
    INCLUDE_DIRS "." "../include"
    EMBED_TXTFILES "../certs/hivemq.crt"
    REQUIRES esp-tls esp_wifi nvs_flash esp_event esp_netif mqtt 
)