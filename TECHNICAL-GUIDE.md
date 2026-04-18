# Technical Details & API Reference

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│           Browser (HTML/CSS/JavaScript)         │
│  - Renders dashboard UI                         │
│  - Handles user clicks on tiles                 │
│  - Polls for device updates every N seconds     │
└────────────────┬────────────────────────────────┘
                 │ HTTPS/API Calls
                 │ (with OAuth token)
┌────────────────▼────────────────────────────────┐
│         Hubitat Custom Dashboard App            │
│  - Groovy code running on hub                   │
│  - Exposes HTTP endpoints (mappings)            │
│  - Manages device selection                     │
│  - Sends commands to devices                    │
└────────────────┬────────────────────────────────┘
                 │ Internal Hub API
┌────────────────▼────────────────────────────────┐
│           Device Drivers on Hub                 │
│  - Z-Wave, Zigbee, WiFi devices                │
│  - Executes on(), off(), setLevel(), etc.       │
└─────────────────────────────────────────────────┘
```

## API Endpoints

### GET /dashboard
Returns the HTML dashboard interface.

**Parameters:**
- `access_token` (required): OAuth access token

**Response:** HTML page

---

### GET /api/devices
Returns list of all configured devices with current status.

**Parameters:**
- `access_token` (required): OAuth access token

**Response:** JSON array of devices
```json
[
  {
    "id": 123,
    "name": "Living Room Light",
    "type": "switch",
    "status": "on",
    "statusText": "On",
    "capabilities": ["Switch"]
  },
  {
    "id": 456,
    "name": "Kitchen Dimmer",
    "type": "dimmer",
    "status": "on",
    "level": 75,
    "statusText": "On (75%)",
    "capabilities": ["Switch", "SwitchLevel"]
  }
]
```

---

### POST /api/command/:id/:command
Sends a command to a specific device.

**Parameters:**
- `id` (path): Device ID
- `command` (path): Command name (e.g., "on", "off", "lock", "unlock")
- `access_token` (query): OAuth access token

**Response:** JSON
```json
{"success": true}
```

**Example:**
```
POST /api/command/123/on?access_token=abc123
```

---

### POST /api/setLevel/:id/:level
Sets the brightness level for a dimmer.

**Parameters:**
- `id` (path): Device ID
- `level` (path): Level (0-100)
- `access_token` (query): OAuth access token

**Response:** JSON
```json
{"success": true}
```

**Example:**
```
POST /api/setLevel/456/75?access_token=abc123
```

## How the "Click Anywhere" Feature Works

### CSS Magic

The key is in the CSS styling of the tiles:

```css
.tile {
    cursor: pointer;           /* Shows it's clickable */
    user-select: none;         /* Prevents text selection */
    /* Makes entire tile a click target */
}

.tile:hover {
    transform: translateY(-4px); /* Visual feedback */
}

.tile:active {
    transform: translateY(-2px); /* Press feedback */
}
```

### JavaScript Event Handling

```javascript
tile.addEventListener('click', (e) => handleTileClick(device, e));
```

The entire `<div class="tile">` element is the click target. When you click anywhere within the tile boundaries, the event fires.

### Slider Exception

For dimmers, we prevent the slider from triggering the tile click:

```javascript
function handleTileClick(device, event) {
    // Prevent slider events from triggering tile click
    if (event.target.classList.contains('dimmer-slider')) {
        return;
    }
    // ... rest of the logic
}
```

This way:
- Clicking the tile → toggles on/off
- Dragging the slider → adjusts brightness
- No conflicts!

## Device Capability Mapping

| Hubitat Capability | Commands Available | Attributes Read |
|-------------------|-------------------|-----------------|
| Switch | on(), off() | switch (on/off) |
| SwitchLevel | on(), off(), setLevel(n) | switch, level (0-100) |
| Lock | lock(), unlock() | lock (locked/unlocked) |
| ContactSensor | (none - read only) | contact (open/closed) |
| MotionSensor | (none - read only) | motion (active/inactive) |
| Thermostat | (not implemented yet) | temperature, thermostatMode |

## Adding New Device Types

To add support for more device types:

### 1. Add Input in Preferences

```groovy
preferences {
    section("Select Devices") {
        // ... existing inputs ...
        input "colorBulbs", "capability.colorControl", 
              title: "Select Color Bulbs", 
              multiple: true, 
              required: false
    }
}
```

### 2. Subscribe to Events

```groovy
def initialize() {
    // ... existing subscriptions ...
    if (colorBulbs) subscribe(colorBulbs, "color", deviceHandler)
}
```

### 3. Add to getDevices()

```groovy
def getDevices() {
    // ... existing device types ...
    
    colorBulbs?.each { device ->
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "color",
            status: device.currentValue("switch"),
            color: device.currentValue("color"),
            statusText: "Color: ${device.currentValue("color")}",
            capabilities: ["Switch", "ColorControl"]
        ]
    }
}
```

### 4. Add Command Endpoint (if needed)

```groovy
mappings {
    // ... existing mappings ...
    path("/api/setColor/:id/:color") {
        action: [POST: "setColor"]
    }
}

def setColor() {
    def deviceId = params.id
    def color = params.color
    def device = findDevice(deviceId)
    if (device) {
        device.setColor(color)
        render contentType: "application/json", data: '{"success":true}'
    }
}
```

### 5. Update JavaScript

```javascript
function handleTileClick(device, event) {
    if (device.capabilities.includes('ColorControl')) {
        // Show color picker or toggle
        showColorPicker(device);
    }
    // ... existing logic ...
}
```

## Styling Customization Examples

### Change Tile Colors

```css
/* Make "on" tiles green instead of purple */
.tile.on {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    color: white;
}

/* Custom locked color */
.tile.locked {
    background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
    color: white;
}
```

### Change Tile Spacing and Shape

```css
.dashboard-grid {
    gap: 30px;  /* More space between tiles */
}

.tile {
    border-radius: 20px;  /* More rounded corners */
    padding: 25px;        /* More internal padding */
}
```

### Add Tile Borders

```css
.tile {
    border: 3px solid rgba(255, 255, 255, 0.2);
}

.tile.on {
    border: 3px solid rgba(255, 255, 255, 0.5);
}
```

## Performance Optimization

### Reduce Polling Frequency

For devices that don't change often:

```javascript
const REFRESH_INTERVAL = 10 * 1000; // 10 seconds instead of 5
```

### Event-Driven Updates (Advanced)

Instead of polling, you could implement WebSocket connections:

1. Add WebSocket support in Groovy
2. Push events when devices change
3. Update UI instantly on the browser

This requires more complex implementation but eliminates polling delays.

### Lazy Loading

For dashboards with 50+ devices:

```javascript
// Only render visible tiles (virtual scrolling)
// Load additional tiles as user scrolls
```

## Security Considerations

### Access Token Protection

The access token grants full control over selected devices:

1. **Never share** the dashboard URL publicly
2. **Regenerate tokens** if compromised (reinstall app)
3. **Use local URLs** when possible (not exposed to internet)
4. **Enable hub firewall** to restrict local network access

### HTTPS Only

- Hub enforces HTTPS for all API calls
- Self-signed certificate on local connections
- Valid certificate on cloud connections

### URL Encoding

Access tokens are URL-encoded to prevent injection attacks.

## Debugging

### View App Logs

1. Apps > Custom Dashboard instance
2. Click gear icon
3. View app logs

### Browser Console

Press F12 in browser to see:
- API requests/responses
- JavaScript errors
- Network timing

### Common Issues

**"Failed to fetch devices"**
- Check OAuth is enabled
- Verify access token in URL
- Check hub network connectivity

**Commands don't work**
- Verify device capabilities match commands
- Check device responsiveness in main interface
- Review app logs for error messages

**Slow response**
- Reduce refresh interval
- Check hub CPU usage
- Upgrade to local URL if using cloud

## Advanced: Custom Commands

Add buttons for scenes or routines:

```javascript
// Add a "Good Night" button
const goodNightTile = `
    <div class="tile special" onclick="runGoodNight()">
        <div class="tile-icon">🌙</div>
        <div class="tile-name">Good Night</div>
    </div>
`;

async function runGoodNight() {
    // Turn off multiple devices
    await sendCommand(123, 'off');  // Living room light
    await sendCommand(456, 'off');  // Kitchen light
    await sendCommand(789, 'lock'); // Front door
}
```

## Browser Compatibility

Tested on:
- ✅ Chrome/Edge (Windows, Mac, Android)
- ✅ Safari (iOS, macOS)
- ✅ Firefox (Windows, Mac)
- ✅ Samsung Internet (Android)

Requires:
- Modern browser (ES6+ JavaScript support)
- Fetch API support
- CSS Grid support

All modern browsers from 2018+ support these features.

## License

This is custom code - use and modify as you wish!

## Credits

Created to solve the "entire tile clickable" problem for Hubitat dashboards.
