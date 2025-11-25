# rpi-person-temp-monitor

Publishes To
* roomA/temperature/status
temp_payload = {
                    "temp_f": round(temp_f, 1),
                    "timestamp": timestamp
                }

* roomA/person/status
payload = {
                "status": status,
                "confidence": round(conf, 4),
                "timestamp": now_iso()
            }

<!-- * roomA/person/frame -->


