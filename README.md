# ThermaLink

## MQTT Network

```bash
roomA/temperature/target        # App pubs, heater subs
roomA/temperature/status        # Pi pubs, heater & app subs
roomA/person/status             # Pi pubs, heater & app subs
roomA/person/frame              # Pi pubs, app subs
roomA/heater/status             # heater pubs, app subs
roomA/heater/command            # app pubs, heater subs
roomA/fan/status                # heater pubs, app subs
roomA/fan/command               # app pubs, heater subs

baseline/temperature/status     # base pubs, app subs
```