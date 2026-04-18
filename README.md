# Hubitat Custom Dashboard

A custom smart home dashboard for [Hubitat Elevation](https://hubitat.com/) with fully clickable tiles and a modern, responsive interface. Also includes a Tuya IoT Cloud integration driver.

## Project Contents

| File | Description |
|------|-------------|
| `custom-dashboard-app.groovy` | Custom dashboard Hubitat app — the main project |
| `TuyaOpenCloudAPI.groovy` | Tuya IoT Platform (Cloud) driver for Hubitat |
| `INSTALLATION-GUIDE.md` | Step-by-step installation and usage instructions |
| `TECHNICAL-GUIDE.md` | Architecture, API reference, and developer guide |

---

## Custom Dashboard App

### Features

- **Entire tile is clickable** — no need to hit tiny buttons, tap anywhere on a tile to toggle devices
- Modern, responsive design that works on desktop, tablet, and mobile
- Drag-and-drop tile reordering (edit mode)
- Customizable appearance — background color, tile colors (solid or gradient), text color, icon size, text alignment
- Dark mode support
- Smart adaptive polling — faster refresh after user interaction, slower when idle
- Incremental tile updates — only changed tiles re-render for smooth performance
- Color picker and color temperature slider for RGB/CT bulbs
- Local and cloud access URLs

### Supported Device Types

| Category | Device Type | Controls |
|----------|------------|----------|
| **Controllable** | Switches / Lights | Toggle on/off, color picker, color temperature slider |
| | Dimmers | Toggle on/off, brightness slider (0–100%) |
| | Window Shades / Blinds | Toggle open/close, position slider (0–100%) |
| | Locks | Toggle lock/unlock |
| **Sensors** | Contact Sensors | Displays open/closed (read-only) |
| | Motion Sensors | Displays active/inactive (read-only) |
| | Temperature Sensors | Displays temperature reading |
| | Humidity Sensors | Displays humidity percentage |
| | Illuminance Sensors | Displays lux value |
| | Water/Leak Sensors | Displays dry/wet with alert animation |
| | Smoke Detectors | Displays clear/detected with alert animation |
| | CO Detectors | Displays clear/detected with alert animation |
| **Other** | Thermostats | Displays temperature and mode (read-only) |

### Invert Display Logic

Devices can be added to an "Invert" list so their highlighting logic is reversed. For example, blinds can be highlighted when **closed** instead of when open — useful when your preferred state is the closed state.

### Quick Start

1. **Add the app code** — In Hubitat, go to **Developer Tools > Apps Code > + New App**, paste the contents of `custom-dashboard-app.groovy`, and click **Save**
2. **Enable OAuth** — In the Apps Code editor, click **OAuth > Enable OAuth in App > Update**, then **Save** again
3. **Install the app** — Go to **Apps > Add User App**, select "Custom Dashboard"
4. **Configure** — Name your dashboard, select devices, set preferences, click **Done**
5. **Open the dashboard** — Copy the Local or Cloud URL shown on the app page

> **OAuth must be enabled or the dashboard will not work.**

### Dashboard Settings

| Setting | Description | Default |
|---------|-------------|---------|
| Auto-refresh interval | How often to poll for device updates (seconds) | 5 |
| Tile Size | Small, Medium, or Large tiles | Medium |
| Dark Mode | Dark background theme | Off |

### Edit Mode

Click the **Edit** button in the top-right corner to:

- **Drag and drop** tiles to rearrange them on the 6-column grid
- **Customize colors** — background, tile on/off colors (solid or gradient), text color
- **Adjust icon size** globally
- **Change text alignment** (left, center, right)
- Leave empty grid cells for spacing

Click **Done** to save your layout and return to normal mode.

### Access Methods

- **Local URL** — Fast, works within your home network only
- **Cloud URL** — Works remotely via Hubitat's cloud relay
- **Home screen shortcut** — Add the URL to your phone/tablet home screen for app-like access
- **Multiple dashboards** — Install the app multiple times for room-specific dashboards

---

## Tuya IoT Platform (Cloud) Driver

A Hubitat device driver (`TuyaOpenCloudAPI.groovy`) that integrates Tuya-based smart devices via the Tuya Open Cloud API.

### Supported Tuya Devices

- Switches, dimmers, and smart plugs
- RGB and color temperature lights
- Window shades/covers
- Contact, motion, temperature, humidity, CO2, brightness sensors
- Smoke, CO, water, and vibration sensors
- Fans and humidifiers
- Locks and scene switches
- Pet feeders
- Power strips with individual outlet control
- Tuya scene triggering

### Setup

1. **Add the driver code** — In Hubitat, go to **Developer Tools > Drivers Code > + New Driver**, paste the contents of `TuyaOpenCloudAPI.groovy`, and click **Save**
2. **Create a virtual device** — Go to **Devices > Add Device > Virtual**, select "Tuya IoT Platform (Cloud)" as the driver
3. **Configure credentials** — Enter your Tuya API Access/Client ID, Client Secret, Tuya app login/password, app type (Tuya Smart or Smart Life), and country
4. Click **Initialize** to connect

Credentials are entered as runtime preferences (masked password fields) — nothing is hardcoded in the source.

---

## API Reference

The dashboard app exposes these HTTP endpoints (all require `access_token` query parameter):

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Renders the HTML dashboard |
| GET | `/api/devices` | Returns all devices with current status (JSON) |
| POST | `/api/command/:id/:command` | Sends a command to a device (on, off, lock, unlock, open, close) |
| POST | `/api/setLevel/:id/:level` | Sets dimmer brightness (0–100) |
| POST | `/api/setPosition/:id/:position` | Sets window shade position (0–100) |
| POST | `/api/setColor/:id` | Sets RGB color (JSON body: `{"hex": "#ff0000"}`) |
| POST | `/api/setColorTemp/:id/:kelvin` | Sets color temperature (2000–6500K) |
| POST | `/api/saveLayout` | Saves tile grid layout |
| GET | `/api/getGridLayout` | Retrieves saved grid layout |
| POST | `/api/saveCustomColors` | Saves appearance customizations |
| GET | `/api/getCustomColors` | Retrieves saved appearance customizations |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Dashboard stuck on "Loading devices..." | Verify OAuth is enabled; check the URL includes a valid `access_token` |
| Devices don't respond to clicks | Confirm devices are selected in app settings; check device status on the Hubitat Devices page |
| Dashboard doesn't auto-update | Check refresh interval setting; verify network connectivity |
| Cloud URL not working | Ensure hub is registered with Hubitat cloud and cloud access is enabled |
| Tiles look wrong after update | Enter Edit mode and click Done to re-save layout |

For detailed logs, go to **Apps > Custom Dashboard > View Logs** in the Hubitat interface.

---

## License

- `custom-dashboard-app.groovy` — by Mark Colatosti
- `TuyaOpenCloudAPI.groovy` — MIT License, Copyright 2020 Jonathan Bradshaw (jb@nrgup.net)
