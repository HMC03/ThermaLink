#pragma once
#include <stdbool.h>

// Initialize relay GPIOs
void relay_init(void);

// Main HVAC decision logic
void relay_update(float current_temp,
                  float target_temp,
                  bool person_present);

// Force individual control
void relay_set_heater(bool on);
void relay_set_fan(bool on);

// Query state
bool relay_get_heater(void);
bool relay_get_fan(void);
