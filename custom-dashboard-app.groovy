/**
 *  Custom Dashboard App
 *
 *  A custom dashboard with enhanced clickable areas for Hubitat Elevation
 *  
 *  Features:
 *  - Entire tile is clickable (no need to hit specific buttons)
 *  - Clean, modern interface
 *  - Support for switches, dimmers, window shades, locks, and thermostats
 *  - Real-time device status updates
 *
 *  Instructions:
 *  1. Add this code to Apps Code in Hubitat
 *  2. Save the app
 *  3. Go to the app settings and enable OAuth
 *  4. Install from Apps > Add User App
 *  5. Select devices to include
 *  6. Copy the dashboard URL from the app page
 */

definition(
    name: "Custom Dashboard",
    namespace: "markcolatosti",
    author: "Mark Colatosti",
    description: "Enhanced custom dashboard with fully clickable tiles",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    oauth: true,
    singleInstance: false
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Custom Dashboard Setup", install: true, uninstall: true) {
        section("Dashboard Name") {
            label title: "Dashboard Name", required: true
        }
        
        section("Select Devices") {
            input "switches", "capability.switch", title: "Select Switches/Lights", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Select Dimmers", multiple: true, required: false
            input "shades", "capability.windowShade", title: "Select Window Shades/Blinds", multiple: true, required: false
            input "locks", "capability.lock", title: "Select Locks", multiple: true, required: false
            input "thermostats", "capability.thermostat", title: "Select Thermostats", multiple: true, required: false
        }
        
        section("Invert Display Logic") {
            paragraph "Select devices to invert display highlighting (e.g., blinds highlighted when closed instead of open)"
            input "invertDevices", "capability.*", title: "Select Devices to Invert", multiple: true, required: false
        }
        
        section("Select Sensors") {
            input "contactSensors", "capability.contactSensor", title: "Select Contact Sensors (Doors/Windows)", multiple: true, required: false
            input "motionSensors", "capability.motionSensor", title: "Select Motion Sensors", multiple: true, required: false
            input "temperatureSensors", "capability.temperatureMeasurement", title: "Select Temperature Sensors", multiple: true, required: false
            input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Select Humidity Sensors", multiple: true, required: false
            input "illuminanceSensors", "capability.illuminanceMeasurement", title: "Select Light/Illuminance Sensors", multiple: true, required: false
            input "waterSensors", "capability.waterSensor", title: "Select Water/Leak Sensors", multiple: true, required: false
            input "smokeSensors", "capability.smokeDetector", title: "Select Smoke Detectors", multiple: true, required: false
            input "coSensors", "capability.carbonMonoxideDetector", title: "Select CO Detectors", multiple: true, required: false
        }
        
        section("Dashboard Settings") {
            input "refreshInterval", "number", title: "Auto-refresh interval (seconds)", defaultValue: 5, required: false
            input "tileSize", "enum", title: "Tile Size", options: ["small", "medium", "large"], defaultValue: "medium", required: false
            input "darkMode", "bool", title: "Enable Dark Mode", defaultValue: false, required: false
        }
        
        section("Dashboard Access") {
            if (state.accessToken) {
                paragraph "Dashboard URL (Local):"
                paragraph "<a href='${getFullLocalApiServerUrl()}/dashboard?access_token=${state.accessToken}' target='_blank'>${getFullLocalApiServerUrl()}/dashboard?access_token=${state.accessToken}</a>"
                paragraph "\nDashboard URL (Cloud):"
                paragraph "<a href='${getFullApiServerUrl()}/dashboard?access_token=${state.accessToken}' target='_blank'>${getFullApiServerUrl()}/dashboard?access_token=${state.accessToken}</a>"
            } else {
                paragraph "Save the app to generate dashboard URLs"
            }
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    if (!state.accessToken) {
        createAccessToken()
    }
    
    // Subscribe to all device events for real-time updates
    if (switches) subscribe(switches, "switch", deviceHandler)
    if (dimmers) subscribe(dimmers, "level", deviceHandler)
    if (shades) {
        subscribe(shades, "position", deviceHandler)
        subscribe(shades, "windowShade", deviceHandler)
    }
    if (locks) subscribe(locks, "lock", deviceHandler)
    if (thermostats) subscribe(thermostats, "temperature", deviceHandler)
    if (contactSensors) subscribe(contactSensors, "contact", deviceHandler)
    if (motionSensors) subscribe(motionSensors, "motion", deviceHandler)
    if (temperatureSensors) subscribe(temperatureSensors, "temperature", deviceHandler)
    if (humiditySensors) subscribe(humiditySensors, "humidity", deviceHandler)
    if (illuminanceSensors) subscribe(illuminanceSensors, "illuminance", deviceHandler)
    if (waterSensors) subscribe(waterSensors, "water", deviceHandler)
    if (smokeSensors) subscribe(smokeSensors, "smoke", deviceHandler)
    if (coSensors) subscribe(coSensors, "carbonMonoxide", deviceHandler)
}

def deviceHandler(evt) {
    log.debug "Device event: ${evt.device} ${evt.name} = ${evt.value}"
}

mappings {
    path("/dashboard") {
        action: [GET: "renderDashboard"]
    }
    path("/api/devices") {
        action: [GET: "getDevices"]
    }
    path("/api/command/:id/:command") {
        action: [POST: "sendCommand"]
    }
    path("/api/setLevel/:id/:level") {
        action: [POST: "setLevel"]
    }
    path("/api/setPosition/:id/:position") {
        action: [POST: "setPosition"]
    }
    path("/api/saveLayout") {
        action: [POST: "saveLayout"]
    }
    path("/api/getGridLayout") {
        action: [GET: "getGridLayout"]
    }
    path("/api/setColor/:id") {
        action: [POST: "setColor"]
    }
    path("/api/setColorTemp/:id/:kelvin") {
        action: [POST: "setColorTemp"]
    }
    path("/api/getCustomColors") {
        action: [GET: "getCustomColors"]
    }
    path("/api/saveCustomColors") {
        action: [POST: "saveCustomColors"]
    }
}

def renderDashboard() {
    def html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${app.label ?: 'Custom Dashboard'}</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: ${darkMode ? '#1a1a1a' : '#f5f5f5'};
            color: ${darkMode ? '#ffffff' : '#333333'};
            padding: 20px;
        }
        
        .header {
            text-align: center;
            margin-bottom: 30px;
            position: relative;
        }
        
        .header h1 {
            font-size: 2em;
            margin-bottom: 10px;
            display: none;
        }
        
        .header h1.visible {
            display: block;
        }
        
        .edit-button {
            position: absolute;
            top: 0;
            right: 20px;
            padding: 10px 20px;
            background: ${darkMode ? '#444' : '#667eea'};
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9em;
            font-weight: 600;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .edit-button:hover {
            background: ${darkMode ? '#555' : '#764ba2'};
        }
        
        .edit-button.active {
            background: #f5576c;
        }
        
        .edit-icon {
            font-size: 1.2em;
        }
        
        .customize-panel {
            background: ${darkMode ? '#2d2d2d' : '#ffffff'};
            border-radius: 12px;
            padding: 20px;
            margin: 20px 20px 20px auto;
            max-width: 600px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.2);
            display: none;
        }
        
        .customize-panel.visible {
            display: block;
        }
        
        .customize-panel h2 {
            margin-bottom: 20px;
            font-size: 1.3em;
        }
        
        .color-option {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 15px;
            padding: 10px;
            background: ${darkMode ? '#1a1a1a' : '#f5f5f5'};
            border-radius: 8px;
        }
        
        .color-option label {
            font-weight: 600;
            flex: 1;
        }
        
        .color-option input[type="color"] {
            width: 60px;
            height: 40px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
        }
        
        .color-option button {
            margin-left: 10px;
            padding: 8px 15px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.85em;
            transition: background 0.3s ease;
        }
        
        .color-option button:hover {
            background: #764ba2;
        }
        
        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 20px;
            max-width: 1400px;
            margin: 0 auto;
            min-height: 400px;
        }
        
        .empty-cell {
            background: transparent;
            border: 2px dashed transparent;
            border-radius: 12px;
            min-height: ${tileSize == 'small' ? '120px' : tileSize == 'large' ? '180px' : '150px'};
            transition: all 0.3s ease;
            /* Force exact same width as tiles */
            width: 100%;
            min-width: 0;
            max-width: 100%;
        }
        
        .empty-cell.drag-over {
            border-color: #667eea;
            background: rgba(102, 126, 234, 0.1);
        }
        
        /* THE KEY FEATURE: Entire tile is clickable! */
        .tile {
            background: ${darkMode ? '#2d2d2d' : '#ffffff'};
            border-radius: 12px;
            padding: 20px;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            min-height: ${tileSize == 'small' ? '120px' : tileSize == 'large' ? '180px' : '150px'};
            position: relative;
            user-select: none;
            /* This makes the ENTIRE tile respond to clicks */
            -webkit-tap-highlight-color: transparent;
            /* Force all tiles to exact same width */
            width: 100%;
            min-width: 0;
            max-width: 100%;
        }
        
        .tile:hover {
            box-shadow: 0 4px 16px rgba(0,0,0,0.2);
        }
        
        .tile:active {
            box-shadow: 0 2px 12px rgba(0,0,0,0.15);
        }
        
        .tile.draggable {
            cursor: move;
        }
        
        .tile.dragging {
            opacity: 0.3;
        }
        
        .tile.drag-over {
            box-shadow: 0 0 20px rgba(102, 126, 234, 0.8);
            transform: scale(1.02);
        }
        
        .tile.on {
            /* Color set dynamically based on background luminance */
        }
        
        .tile.off {
            /* Opacity removed - color set dynamically via inline styles */
        }
        
        .tile-icon {
            font-size: 2.5em;
            margin-bottom: 10px;
            transition: font-size 0.2s ease;
            text-align: left;
        }
        
        .tile-name {
            font-size: 1.1em;
            font-weight: 600;
            margin-bottom: 8px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        
        .tile-status {
            font-size: 0.9em;
            opacity: 0.8;
        }
        
        .tile.sensor .tile-status {
            font-size: 1.4em;
            font-weight: 600;
        }
        
        .tile.locked {
            /* Background and color set dynamically via inline styles */
        }
        
        .tile.unlocked {
            /* Background and color set dynamically via inline styles */
        }
        
        .tile.open {
            /* Background and color set dynamically via inline styles */
        }
        
        .tile.closed {
            /* Background and color set dynamically via inline styles */
        }
        
        .tile.active {
            /* Background and color set dynamically via inline styles */
        }
        
        .tile.inactive {
            /* Background, color, and opacity set dynamically via inline styles */
        }
        
        .tile.sensor {
            /* Background set dynamically via inline styles */
            cursor: default;
        }
        
        .tile.sensor:hover {
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        
        .tile.alert {
            /* Background and color set dynamically via inline styles */
            animation: pulse 2s ease-in-out infinite;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.7; }
        }
        
        /* Color picker - fully circular custom appearance */
        .color-picker-container {
            position: absolute;
            top: 12px;
            right: 12px;
            width: 38px;
            height: 38px;
            cursor: pointer;
            z-index: 10;
        }
        
        .color-picker-input {
            position: absolute;
            opacity: 0;
            width: 0;
            height: 0;
            pointer-events: none;
        }
        
        .color-picker-display {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            border: 3px solid rgba(255, 255, 255, 0.9);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            cursor: pointer;
        }
        
        .color-picker-display:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
        }
        
        /* Color temperature slider */
        .color-temp-slider {
            width: 100%;
            margin-top: 10px;
            margin-bottom: 15px;
            -webkit-appearance: none;
            appearance: none;
            height: 6px;
            border-radius: 3px;
            background: linear-gradient(to right, #ff9329, #ffefba, #87ceeb);
            outline: none;
        }
        
        .color-temp-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: white;
            border: 2px solid #666;
            cursor: pointer;
        }
        
        .color-temp-slider::-moz-range-thumb {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: white;
            border: 2px solid #666;
            cursor: pointer;
        }
        
        .temp-label {
            font-size: 0.8em;
            opacity: 0.7;
        }
        
        /* Dimmer slider */
        .dimmer-slider {
            width: 100%;
            margin-top: 10px;
            -webkit-appearance: none;
            appearance: none;
            height: 6px;
            border-radius: 3px;
            background: ${darkMode ? '#444' : '#ddd'};
            outline: none;
        }
        
        .dimmer-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
        }
        
        .dimmer-slider::-moz-range-thumb {
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
            border: none;
        }
        
        /* Icon size slider (edit mode only) */
        .icon-size-control {
            margin-top: 12px;
            padding-top: 8px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }
        
        .icon-size-slider {
            width: 100%;
            -webkit-appearance: none;
            appearance: none;
            height: 4px;
            border-radius: 2px;
            background: ${darkMode ? '#555' : '#ccc'};
            outline: none;
        }
        
        .icon-size-slider::-webkit-slider-thumb {
            -webkit-appearance: none;
            appearance: none;
            width: 14px;
            height: 14px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
        }
        
        .icon-size-slider::-moz-range-thumb {
            width: 14px;
            height: 14px;
            border-radius: 50%;
            background: #667eea;
            cursor: pointer;
            border: none;
        }
        
        .icon-size-label {
            font-size: 0.75em;
            opacity: 0.6;
            text-align: center;
            margin-top: 4px;
        }
        
        .loading {
            text-align: center;
            padding: 40px;
            font-size: 1.2em;
        }
        
        .error {
            color: #f5576c;
            text-align: center;
            padding: 20px;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1 id="header-title">${app.label ?: 'Custom Dashboard'}</h1>
        <p id="header-subtitle" style="display: none;">Click anywhere on a tile to control it</p>
        <button id="editButton" class="edit-button">
            <span class="edit-icon">�</span>
            <span>Edit</span>
        </button>
    </div>
    
    <div id="customizePanel" class="customize-panel">
        <h2>Customize Appearance</h2>
        <div class="color-option">
            <label>Background Color</label>
            <input type="color" id="bgColor" value="#f5f5f5">
            <button onclick="resetColor('background')">Reset</button>
        </div>
        <div class="color-option">
            <label>Tile Off - Color 1 (use same for solid)</label>
            <input type="color" id="tileOffColor1" value="#ffffff">
            <button onclick="resetColor('tileOff1')">Reset</button>
        </div>
        <div class="color-option">
            <label>Tile Off - Color 2 (gradient end)</label>
            <input type="color" id="tileOffColor2" value="#ffffff">
            <button onclick="resetColor('tileOff2')">Reset</button>
        </div>
        <div class="color-option">
            <label>Tile On - Color 1 (use same for solid)</label>
            <input type="color" id="tileOnColor1" value="#667eea">
            <button onclick="resetColor('tileOn1')">Reset</button>
        </div>
        <div class="color-option">
            <label>Tile On - Color 2 (gradient end)</label>
            <input type="color" id="tileOnColor2" value="#764ba2">
            <button onclick="resetColor('tileOn2')">Reset</button>
        </div>
        <div class="color-option">
            <label>Text Color (leave default for auto)</label>
            <input type="color" id="textColor" value="#ffffff">
            <button onclick="resetColor('textColor')">Auto</button>
        </div>
        <div class="color-option">
            <label>Icon Size</label>
            <input type="range" id="iconSize" min="1.0" max="5.0" step="0.1" value="2.5" style="width: 120px;">
            <span id="iconSizeLabel" style="margin-left: 10px; min-width: 50px; display: inline-block;">2.5em</span>
            <button onclick="resetIconSize()">Reset</button>
        </div>
        <div class="color-option">
            <label>Text Alignment</label>
            <select id="textAlign" style="padding: 8px; border-radius: 6px; border: 1px solid #ccc; background: white; cursor: pointer;">
                <option value="left">Left</option>
                <option value="center">Center</option>
                <option value="right">Right</option>
            </select>
            <button onclick="resetTextAlign()">Reset</button>
        </div>
    </div>
    
    <div id="loading" class="loading">Loading devices...</div>
    <div id="error" class="error" style="display: none;"></div>
    <div id="dashboard" class="dashboard-grid"></div>
    
    <script>
        const API_BASE = window.location.origin + window.location.pathname.replace('/dashboard', '');
        const ACCESS_TOKEN = new URLSearchParams(window.location.search).get('access_token');
        const REFRESH_INTERVAL = ${refreshInterval ?: 5} * 1000;
        const FAST_REFRESH_INTERVAL = 2000; // 2 seconds after user interaction
        const FAST_REFRESH_DURATION = 30000; // Stay fast for 30 seconds
        
        console.log('Dashboard initialized');
        console.log('API_BASE:', API_BASE);
        console.log('ACCESS_TOKEN:', ACCESS_TOKEN ? 'Present (length: ' + ACCESS_TOKEN.length + ')' : 'MISSING!');
        console.log('REFRESH_INTERVAL:', REFRESH_INTERVAL);
        console.log('FAST_REFRESH_INTERVAL:', FAST_REFRESH_INTERVAL);
        
        let devices = [];
        let previousDevices = {};  // Store previous device states for change detection
        let tileElements = {};  // Cache tile DOM elements by device ID
        let gridLayout = {};  // Maps device ID to {row, col}
        let gridColumns = 6;
        let editMode = false;
        let draggedElement = null;
        let draggedDeviceId = null;
        let lastUserActivity = Date.now();
        let pollIntervalId = null;
        let colorPickerOpen = false;
        let colorPickerTimeout = null;
        let sliderActive = false;
        let sliderActiveTimeout = null;  // Safety timeout for sliderActive
        let programmaticUpdate = false;  // Flag to prevent event loops from programmatic updates
        let customColors = {
            background: '${darkMode ? '#1a1a1a' : '#f5f5f5'}',
            tileOff1: '${darkMode ? '#2d2d2d' : '#ffffff'}',
            tileOff2: '${darkMode ? '#2d2d2d' : '#ffffff'}',
            tileOn1: '#667eea',
            tileOn2: '#764ba2',
            textColor: 'auto',
            iconSize: 2.5,
            textAlign: 'center'
        };
        
        // Global mouseup/touchend listeners to ensure sliderActive is always cleared
        // This fixes the issue where mouseup doesn't fire if you move off the slider before releasing
        document.addEventListener('mouseup', () => {
            if (sliderActive) {
                sliderActive = false;
                if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
            }
        });
        
        document.addEventListener('touchend', () => {
            if (sliderActive) {
                sliderActive = false;
                if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
            }
        });
        
        // Page visibility detection - pause polling when tab hidden
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                console.log('Tab hidden, pausing polling');
                if (pollIntervalId) {
                    clearInterval(pollIntervalId);
                    pollIntervalId = null;
                }
            } else {
                console.log('Tab visible, resuming polling');
                fetchDevices(); // Immediate refresh when returning
                restartPolling();
            }
        });
        
        // Mark user activity for smart polling
        function markUserActivity() {
            lastUserActivity = Date.now();
            restartPolling();
        }
        
        // Get current appropriate polling interval
        function getCurrentInterval() {
            const timeSinceActivity = Date.now() - lastUserActivity;
            if (timeSinceActivity < FAST_REFRESH_DURATION) {
                return FAST_REFRESH_INTERVAL;
            }
            return REFRESH_INTERVAL;
        }
        
        // Restart polling with appropriate interval
        function restartPolling() {
            if (pollIntervalId) {
                clearInterval(pollIntervalId);
            }
            
            const interval = getCurrentInterval();
            
            pollIntervalId = setInterval(() => {
                // Don't refresh while color picker is open, sliders are active, or in edit mode
                if (!editMode && !colorPickerOpen && !sliderActive) {
                    fetchDevices();
                    // Check if we need to adjust interval
                    const newInterval = getCurrentInterval();
                    if (newInterval !== interval) {
                        restartPolling();
                    }
                }
            }, interval);
        }
        
        // Check if device state has changed
        function hasDeviceChanged(oldDevice, newDevice) {
            if (!oldDevice) return true;
            return oldDevice.status !== newDevice.status ||
                   oldDevice.level !== newDevice.level ||
                   oldDevice.position !== newDevice.position ||
                   oldDevice.hue !== newDevice.hue ||
                   oldDevice.saturation !== newDevice.saturation ||
                   oldDevice.colorTemperature !== newDevice.colorTemperature ||
                   oldDevice.temperature !== newDevice.temperature ||
                   oldDevice.humidity !== newDevice.humidity ||
                   oldDevice.illuminance !== newDevice.illuminance;
        }
        
        // Fetch all devices from the hub
        async function fetchDevices() {
            const url = API_BASE + '/api/devices?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url);
                
                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error('Failed to fetch devices: ' + response.status);
                }
                
                const newDevices = await response.json();
                
                // Update the display
                document.getElementById('loading').style.display = 'none';
                document.getElementById('error').style.display = 'none';
                
                // Check if we need full re-render or just incremental updates
                const isInitialLoad = devices.length === 0;
                const deviceCountChanged = devices.length !== newDevices.length;
                
                if (isInitialLoad || deviceCountChanged || editMode) {
                    // Full re-render needed
                    devices = newDevices;
                    renderDashboard();
                } else {
                    // Incremental update - only update changed tiles
                    updateChangedTiles(newDevices);
                }
                
                devices = newDevices;
            } catch (error) {
                console.error('Error fetching devices:', error);
                document.getElementById('error').textContent = 'Error loading devices: ' + error.message;
                document.getElementById('error').style.display = 'block';
                document.getElementById('loading').style.display = 'none';
            }
        }
        
        // Send command to device
        async function sendCommand(deviceId, command) {
            const url = API_BASE + '/api/command/' + deviceId + '/' + command + '?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, { method: 'POST' });
                
                if (!response.ok) {
                    throw new Error('Command failed');
                }
                
                // Refresh after short delay for fast feedback
                setTimeout(fetchDevices, 200);
                
                // Mark user activity for smart polling
                markUserActivity();
            } catch (error) {
                console.error('Error sending command:', error);
                alert('Error controlling device: ' + error.message + '\\n\\nCheck browser console (F12) for details.');
            }
        }
        
        // Set dimmer level
        async function setLevel(deviceId, level) {
            const url = API_BASE + '/api/setLevel/' + deviceId + '/' + level + '?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, { method: 'POST' });
                
                if (!response.ok) {
                    throw new Error('Set level failed');
                }
                
                // Refresh to show new level
                setTimeout(fetchDevices, 200);
                
                // Mark user activity for smart polling
                markUserActivity();
            } catch (error) {
                console.error('Error setting level:', error);
                alert('Error setting level: ' + error.message);
            }
        }
        
        // Set window shade position
        async function setPosition(deviceId, position) {
            const url = API_BASE + '/api/setPosition/' + deviceId + '/' + position + '?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, { method: 'POST' });
                
                if (!response.ok) {
                    throw new Error('Set position failed');
                }
                
                // Refresh to show new position
                setTimeout(fetchDevices, 200);
                
                // Mark user activity for smart polling
                markUserActivity();
            } catch (error) {
                console.error('Error setting position:', error);
                alert('Error setting position: ' + error.message);
            }
        }
        
        // Set color for RGB lights
        async function setColor(deviceId, hexColor) {
            const url = API_BASE + '/api/setColor/' + deviceId + '?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ hex: hexColor })
                });
                
                if (!response.ok) {
                    throw new Error('Set color failed');
                }
                
                // Refresh to show new color
                setTimeout(fetchDevices, 200);
                
                // Mark user activity for smart polling
                markUserActivity();
            } catch (error) {
                console.error('Error setting color:', error);
                alert('Error setting color: ' + error.message);
            }
        }
        
        // Set color temperature for tunable white lights
        async function setColorTemp(deviceId, kelvin) {
            const url = API_BASE + '/api/setColorTemp/' + deviceId + '/' + kelvin + '?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, { method: 'POST' });
                
                if (!response.ok) {
                    throw new Error('Set color temp failed');
                }
                
                // Refresh to show new temperature
                setTimeout(fetchDevices, 200);
                
                // Mark user activity for smart polling
                markUserActivity();
            } catch (error) {
                console.error('Error setting color temp:', error);
                alert('Error setting color temperature: ' + error.message);
            }
        }
        
        // Convert HSV to Hex color
        function hsvToHex(hue, saturation) {
            // Hue is 0-100 (Hubitat scale), saturation is 0-100
            const h = hue * 3.6; // Convert to 0-360
            const s = saturation / 100;
            const v = 1; // Assume full value/brightness
            
            const c = v * s;
            const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
            const m = v - c;
            
            let r, g, b;
            if (h < 60) {
                r = c; g = x; b = 0;
            } else if (h < 120) {
                r = x; g = c; b = 0;
            } else if (h < 180) {
                r = 0; g = c; b = x;
            } else if (h < 240) {
                r = 0; g = x; b = c;
            } else if (h < 300) {
                r = x; g = 0; b = c;
            } else {
                r = c; g = 0; b = x;
            }
            
            r = Math.round((r + m) * 255);
            g = Math.round((g + m) * 255);
            b = Math.round((b + m) * 255);
            
            return '#' + [r, g, b].map(x => {
                const hex = x.toString(16);
                return hex.length === 1 ? '0' + hex : hex;
            }).join('');
        }
        
        // Convert Kelvin color temperature to RGB hex
        function kelvinToHex(kelvin) {
            // Clamp to reasonable range
            const temp = Math.max(1000, Math.min(40000, kelvin)) / 100;
            let r, g, b;
            
            // Calculate red
            if (temp <= 66) {
                r = 255;
            } else {
                r = temp - 60;
                r = 329.698727446 * Math.pow(r, -0.1332047592);
                r = Math.max(0, Math.min(255, r));
            }
            
            // Calculate green
            if (temp <= 66) {
                g = temp;
                g = 99.4708025861 * Math.log(g) - 161.1195681661;
            } else {
                g = temp - 60;
                g = 288.1221695283 * Math.pow(g, -0.0755148492);
            }
            g = Math.max(0, Math.min(255, g));
            
            // Calculate blue
            if (temp >= 66) {
                b = 255;
            } else if (temp <= 19) {
                b = 0;
            } else {
                b = temp - 10;
                b = 138.5177312231 * Math.log(b) - 305.0447927307;
                b = Math.max(0, Math.min(255, b));
            }
            
            r = Math.round(r);
            g = Math.round(g);
            b = Math.round(b);
            
            return '#' + [r, g, b].map(x => {
                const hex = x.toString(16);
                return hex.length === 1 ? '0' + hex : hex;
            }).join('');
        }
        
        // Calculate luminance to determine if color is light or dark
        function getTextColor(hexColor) {
            // Convert hex to RGB
            const hex = hexColor.replace('#', '');
            const r = parseInt(hex.substr(0, 2), 16);
            const g = parseInt(hex.substr(2, 2), 16);
            const b = parseInt(hex.substr(4, 2), 16);
            
            // Calculate relative luminance
            const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
            
            // Return black for light backgrounds, white for dark backgrounds
            return luminance > 0.5 ? '#000000' : '#ffffff';
        }
        
        // Get icon for device type
        function getIcon(device) {
            if (device.capabilities.includes('WindowShade') || device.invertShade) return '▤';
            if (device.capabilities.includes('Switch')) return '💡';
            if (device.capabilities.includes('Lock')) return device.status === 'locked' ? '🔒' : '🔑';
            if (device.capabilities.includes('ContactSensor')) return '🚪';
            if (device.capabilities.includes('MotionSensor')) return '👁️';
            if (device.capabilities.includes('Thermostat')) return '🌡️';
            if (device.capabilities.includes('Temperature')) return '🌡️';
            if (device.capabilities.includes('Humidity')) return '💧';
            if (device.capabilities.includes('Illuminance')) return '☀️';
            if (device.capabilities.includes('Water')) return device.status === 'wet' ? '💦' : '✓';
            if (device.capabilities.includes('Smoke')) return device.status === 'detected' ? '🔥' : '✓';
            if (device.capabilities.includes('CarbonMonoxide')) return device.status === 'detected' ? '⚠️' : '✓';
            return '⚡';
        }
        
        // Helper to apply tile colors (ON=true, OFF=false)
        function applyTileColors(tile, isOn) {
            const c1 = isOn ? customColors.tileOn1 : customColors.tileOff1;
            const c2 = isOn ? customColors.tileOn2 : customColors.tileOff2;
            tile.style.background = (c1 === c2) ? c1 : 'linear-gradient(135deg, ' + c1 + ' 0%, ' + c2 + ' 100%)';
            tile.style.color = (customColors.textColor && customColors.textColor !== 'auto') ? customColors.textColor : getTextColor(c1);
        }
        
        // Handle tile click
        function handleTileClick(deviceId, event) {
            // Force clear sliderActive in case it got stuck
            sliderActive = false;
            if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
            
            // Look up current device state (not stale closure data)
            const device = devices.find(d => d.id === deviceId);
            if (!device) {
                console.error('Device not found in current devices array:', deviceId);
                return;
            }
            
            // Prevent slider and color picker events from triggering tile click
            if (event.target.classList.contains('dimmer-slider') || 
                event.target.classList.contains('color-temp-slider') ||
                event.target.classList.contains('temp-label') ||
                event.target.classList.contains('color-picker-input') ||
                event.target.classList.contains('color-picker-display') ||
                event.target.classList.contains('color-picker-container') ||
                event.target.type === 'color') {
                return;
            }
            
            if (device.invertShade && device.capabilities.includes('WindowShade')) {
                // Inverted window shades: Open if closed/partially closed, close if fully open
                sendCommand(device.id, (device.position === 100) ? 'close' : 'open');
            } else if (device.capabilities.includes('WindowShade')) {
                // Normal window shades: Open if closed/partially closed, close if fully open
                sendCommand(device.id, (device.position === 100) ? 'close' : 'open');
            } else if (device.capabilities.includes('Switch')) {
                // Switches (including inverted): toggle on/off
                sendCommand(device.id, device.status === 'on' ? 'off' : 'on');
            } else if (device.capabilities.includes('Lock')) {
                sendCommand(device.id, device.status === 'locked' ? 'unlock' : 'lock');
            }
        }
        
        // Save grid layout to server
        async function saveLayout() {
            const url = API_BASE + '/api/saveLayout?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ positions: gridLayout })
                });
                
                if (!response.ok) {
                    throw new Error('Failed to save layout');
                }
                
                console.log('Grid layout saved successfully', gridLayout);
            } catch (error) {
                console.error('Error saving layout:', error);
                alert('Error saving layout: ' + error.message);
            }
        }
        
        // Load grid layout from server
        async function loadGridLayout() {
            const url = API_BASE + '/api/getGridLayout?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            try {
                const response = await fetch(url);
                if (response.ok) {
                    const layout = await response.json();
                    if (layout && Object.keys(layout).length > 0) {
                        gridLayout = layout;
                    } else {
                        initializeGridLayout();
                    }
                } else {
                    initializeGridLayout();
                }
            } catch (error) {
                console.log('No grid layout saved yet, initializing:', error);
                initializeGridLayout();
            }
        }
        
        // Initialize grid layout from linear order or create new
        function initializeGridLayout() {
            if (devices.length === 0) return;
            
            gridLayout = {};
            let row = 0;
            let col = 0;
            
            devices.forEach((device, index) => {
                gridLayout[device.id] = { row: row, col: col };
                col++;
                if (col >= gridColumns) {
                    col = 0;
                    row++;
                }
            });
            
            console.log('Initialized grid layout:', gridLayout);
        }
        
        // Toggle edit mode
        function toggleEditMode() {
            editMode = !editMode;
            const button = document.getElementById('editButton');
            const title = document.getElementById('header-title');
            const subtitle = document.getElementById('header-subtitle');
            const customizePanel = document.getElementById('customizePanel');
            const icon = button.querySelector('.edit-icon');
            const text = button.querySelector('span:last-child');
            
            if (editMode) {
                icon.textContent = '�';
                text.textContent = 'Done';
                button.classList.add('active');
                title.classList.add('visible');
                subtitle.style.display = 'block';
                subtitle.textContent = 'Drag tiles to any position • Leave empty spaces • Customize colors';
                customizePanel.classList.add('visible');
            } else {
                icon.textContent = '🔒';
                text.textContent = 'Edit';
                button.classList.remove('active');
                title.classList.remove('visible');
                subtitle.style.display = 'none';
                subtitle.textContent = 'Click anywhere on a tile to control it';
                customizePanel.classList.remove('visible');
                saveLayout();
            }
            
            renderDashboard();
        }
        
        // Drag and drop handlers for grid
        function handleDragStart(e, device) {
            draggedElement = e.target;
            draggedDeviceId = device.id.toString();
            e.target.classList.add('dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', device.id);
            console.log('Started dragging device:', draggedDeviceId);
        }
        
        function handleDragOver(e) {
            if (e.preventDefault) {
                e.preventDefault();
            }
            e.dataTransfer.dropEffect = 'move';
            
            // Remove all previous drag-over indicators
            document.querySelectorAll('.drag-over').forEach(el => {
                el.classList.remove('drag-over');
            });
            
            // Get the cell element
            let cell = e.target;
            while (cell && !cell.classList.contains('tile') && !cell.classList.contains('empty-cell')) {
                cell = cell.parentElement;
            }
            
            if (cell && cell !== draggedElement) {
                cell.classList.add('drag-over');
            }
            
            return false;
        }
        
        function handleDrop(e) {
            if (e.preventDefault) {
                e.preventDefault();
            }
            if (e.stopPropagation) {
                e.stopPropagation();
            }
            
            // Get the cell element (tile or empty cell)
            let cell = e.target;
            while (cell && !cell.classList.contains('tile') && !cell.classList.contains('empty-cell')) {
                cell = cell.parentElement;
            }
            
            if (cell && cell !== draggedElement) {
                const targetRow = parseInt(cell.dataset.row);
                const targetCol = parseInt(cell.dataset.col);
                
                if (!isNaN(targetRow) && !isNaN(targetCol)) {
                    // Check if target is occupied by a different device
                    const targetDeviceId = cell.dataset.deviceId;
                    
                    if (targetDeviceId && targetDeviceId !== draggedDeviceId) {
                        // Swap positions
                        const draggedPos = gridLayout[draggedDeviceId];
                        gridLayout[targetDeviceId] = { row: draggedPos.row, col: draggedPos.col };
                        gridLayout[draggedDeviceId] = { row: targetRow, col: targetCol };
                        console.log('Swapped positions');
                    } else {
                        // Move to empty cell
                        gridLayout[draggedDeviceId] = { row: targetRow, col: targetCol };
                        console.log('Moved to empty cell:', targetRow, targetCol);
                    }
                    
                    renderDashboard();
                }
            }
            
            return false;
        }
        
        function handleDragEnd(e) {
            e.target.classList.remove('dragging');
            draggedElement = null;
            draggedDeviceId = null;
            
            // Remove all drag-over indicators
            document.querySelectorAll('.drag-over').forEach(el => {
                el.classList.remove('drag-over');
            });
        }
        
        // Update only tiles that have changed (incremental update)
        function updateChangedTiles(newDevices) {
            newDevices.forEach(newDevice => {
                const oldDevice = previousDevices[newDevice.id];
                if (hasDeviceChanged(oldDevice, newDevice)) {
                    updateSingleTile(newDevice);
                }
            });
            
            // Store new device states
            previousDevices = {};
            newDevices.forEach(device => {
                previousDevices[device.id] = {...device};
            });
        }
        
        // Update a single tile without re-rendering entire dashboard
        function updateSingleTile(device) {
            const tile = tileElements[device.id];
            if (!tile) return; // Tile not rendered yet
            
            // Update status classes and colors
            tile.className = 'tile'; // Reset classes
            
            if (device.invertShade) {
                let isClosed = device.position !== null ? device.position <= 50 : 
                              (device.status === 'closed' || device.status === 'closing');
                tile.classList.add(isClosed ? 'on' : 'off');
                applyTileColors(tile, isClosed);
            } else if (device.capabilities.includes('WindowShade')) {
                let isOpen = device.position !== null ? device.position >= 50 : 
                            (device.status === 'open' || device.status === 'opening');
                tile.classList.add(isOpen ? 'on' : 'off');
                applyTileColors(tile, isOpen);
            } else if (device.capabilities.includes('Switch')) {
                tile.classList.add(device.status === 'on' ? 'on' : 'off');
                applyTileColors(tile, device.status === 'on');
            } else if (device.capabilities.includes('Lock')) {
                tile.classList.add(device.status === 'locked' ? 'locked' : 'unlocked');
                applyTileColors(tile, device.status !== 'locked');
            } else if (device.capabilities.includes('ContactSensor')) {
                tile.classList.add(device.status === 'open' ? 'open' : 'closed');
                applyTileColors(tile, device.status === 'open');
            } else if (device.capabilities.includes('MotionSensor')) {
                tile.classList.add(device.status === 'active' ? 'active' : 'inactive');
                applyTileColors(tile, device.status === 'active');
            } else if (device.capabilities.includes('Water')) {
                tile.classList.add('sensor');
                if (device.status === 'wet') tile.classList.add('alert');
                applyTileColors(tile, device.status === 'wet');
            } else if (device.capabilities.includes('Smoke')) {
                tile.classList.add('sensor');
                if (device.status === 'detected') tile.classList.add('alert');
                applyTileColors(tile, device.status === 'detected');
            } else if (device.capabilities.includes('CarbonMonoxide')) {
                tile.classList.add('sensor');
                if (device.status === 'detected') tile.classList.add('alert');
                applyTileColors(tile, device.status === 'detected');
            } else if (device.capabilities.includes('Temperature') || 
                       device.capabilities.includes('Humidity') || 
                       device.capabilities.includes('Illuminance')) {
                tile.classList.add('sensor');
                applyTileColors(tile, false);
            }
            
            // Update status text
            const statusElement = tile.querySelector('.tile-status');
            if (statusElement) {
                statusElement.textContent = device.statusText;
            }
            
            // Update slider values - be specific about which slider to update
            // Only update sliders if user is not actively dragging them
            if (!sliderActive) {
                programmaticUpdate = true;  // Set flag to prevent change events from firing commands
                
                const colorTempSlider = tile.querySelector('.color-temp-slider');
                if (colorTempSlider && device.colorTemperature !== null) {
                    colorTempSlider.value = device.colorTemperature;
                    const tempLabel = tile.querySelector('.temp-label');
                    if (tempLabel) tempLabel.textContent = device.colorTemperature + 'K';
                }
                
                // Update dimmer/position slider (not color temp slider)
                const dimmerSlider = tile.querySelector('.dimmer-slider');
                if (dimmerSlider) {
                    // For window shades or inverted devices, update position
                    if ((device.capabilities.includes('WindowShade') || device.invertShade) && device.position !== null) {
                        dimmerSlider.value = device.position;
                    }
                    // For regular dimmers/switches with level (not shades), update level
                    else if (device.level !== null && !device.capabilities.includes('WindowShade') && !device.invertShade) {
                        dimmerSlider.value = device.level;
                    }
                }
                
                programmaticUpdate = false;  // Clear flag
            }
            
            // Update color picker display
            const colorDisplay = tile.querySelector('.color-picker-display');
            if (colorDisplay && device.capabilities.includes('ColorControl')) {
                let currentColor;
                if (device.capabilities.includes('ColorTemperature') && device.colorTemperature !== null) {
                    currentColor = kelvinToHex(device.colorTemperature);
                } else if (device.hue !== null && device.saturation !== null) {
                    currentColor = hsvToHex(device.hue, device.saturation);
                }
                if (currentColor) {
                    colorDisplay.style.backgroundColor = currentColor;
                    const colorInput = tile.querySelector('.color-picker-input');
                    if (colorInput) colorInput.value = currentColor;
                }
            }
        }
        
        // Render the dashboard with grid layout
        function renderDashboard() {
            const container = document.getElementById('dashboard');
            container.innerHTML = '';
            tileElements = {}; // Clear tile cache on full re-render
            
            if (devices.length === 0) {
                container.innerHTML = '<div class="loading">No devices configured. Add devices in the app settings.</div>';
                return;
            }
            
            // Calculate grid dimensions
            let maxRow = 0;
            let maxCol = 0;
            
            // Find the maximum row and column used
            Object.values(gridLayout).forEach(pos => {
                if (pos.row > maxRow) maxRow = pos.row;
                if (pos.col > maxCol) maxCol = pos.col;
            });
            
            // Ensure we have at least the minimum grid size
            maxRow = Math.max(maxRow, 2);  // At least 3 rows
            maxCol = Math.max(maxCol, gridColumns - 1);  // At least gridColumns columns
            
            // Create a map of positions to devices
            const positionMap = {};
            devices.forEach(device => {
                const pos = gridLayout[device.id];
                if (pos) {
                    const key = pos.row + '_' + pos.col;
                    positionMap[key] = device;
                }
            });
            
            // Render grid cells
            for (let row = 0; row <= maxRow; row++) {
                for (let col = 0; col <= maxCol; col++) {
                    const key = row + '_' + col;
                    const device = positionMap[key];
                    
                    if (device) {
                        renderTile(device, row, col, container);
                    } else if (editMode) {
                        // Render empty cell in edit mode
                        renderEmptyCell(row, col, container);
                    }
                }
            }
        }
        
        // Render an empty cell
        function renderEmptyCell(row, col, container) {
            const cell = document.createElement('div');
            cell.className = 'empty-cell';
            cell.dataset.row = row;
            cell.dataset.col = col;
            
            // Explicitly position in grid (CSS Grid is 1-indexed)
            cell.style.gridRow = row + 1;
            cell.style.gridColumn = col + 1;
            
            cell.addEventListener('dragover', handleDragOver);
            cell.addEventListener('drop', handleDrop);
            
            container.appendChild(cell);
        }
        
        // Render a tile at specific grid position
        function renderTile(device, row, col, container) {
            const tile = document.createElement('div');
            tile.className = 'tile';
            tile.dataset.deviceId = device.id;
            tile.dataset.row = row;
            tile.dataset.col = col;
            
            // Explicitly position in grid (CSS Grid is 1-indexed)
            tile.style.gridRow = row + 1;
            tile.style.gridColumn = col + 1;
            
            // Apply status classes and custom colors
                if (device.invertShade) {
                    // Inverted logic: ON (highlighted) when closed/closing, OFF when open/opening
                    console.log('Inverted shade:', device.name, 'status:', device.status, 'position:', device.position);
                    let isClosed;
                    if (device.position !== null) {
                        // Use position if available (highlight when MORE closed than open, i.e., position <= 50)
                        isClosed = device.position <= 50;
                    } else {
                        // Fallback to status only if position unavailable
                        isClosed = device.status === 'closed' || device.status === 'closing';
                    }
                    if (isClosed) {
                        tile.classList.add('on');
                        applyTileColors(tile, true);
                    } else {
                        tile.classList.add('off');
                        applyTileColors(tile, false);
                    }
                } else if (device.capabilities.includes('WindowShade')) {
                    // Normal logic: ON (highlighted) when open/opening, OFF when closed/closing
                    console.log('Normal shade:', device.name, 'status:', device.status, 'position:', device.position);
                    let isOpen;
                    if (device.position !== null) {
                        // Use position if available (highlight when MORE open than closed, i.e., position >= 50)
                        isOpen = device.position >= 50;
                    } else {
                        // Fallback to status only if position unavailable
                        isOpen = device.status === 'open' || device.status === 'opening';
                    }
                    if (isOpen) {
                        tile.classList.add('on');
                        applyTileColors(tile, true);
                    } else {
                        tile.classList.add('off');
                        applyTileColors(tile, false);
                    }
                } else if (device.capabilities.includes('Switch')) {
                    if (device.status === 'on') {
                        tile.classList.add('on');
                        applyTileColors(tile, true);
                    } else {
                        tile.classList.add('off');
                        applyTileColors(tile, false);
                    }
                } else if (device.capabilities.includes('Lock')) {
                    tile.classList.add(device.status === 'locked' ? 'locked' : 'unlocked');
                    applyTileColors(tile, device.status !== 'locked');
                } else if (device.capabilities.includes('ContactSensor')) {
                    tile.classList.add(device.status === 'open' ? 'open' : 'closed');
                    applyTileColors(tile, device.status === 'open');
                } else if (device.capabilities.includes('MotionSensor')) {
                    tile.classList.add(device.status === 'active' ? 'active' : 'inactive');
                    applyTileColors(tile, device.status === 'active');
                } else if (device.capabilities.includes('Water')) {
                    tile.classList.add('sensor');
                    if (device.status === 'wet') tile.classList.add('alert');
                    applyTileColors(tile, device.status === 'wet');
                } else if (device.capabilities.includes('Smoke')) {
                    tile.classList.add('sensor');
                    if (device.status === 'detected') tile.classList.add('alert');
                    applyTileColors(tile, device.status === 'detected');
                } else if (device.capabilities.includes('CarbonMonoxide')) {
                    tile.classList.add('sensor');
                    if (device.status === 'detected') tile.classList.add('alert');
                    applyTileColors(tile, device.status === 'detected');
                } else if (device.capabilities.includes('Temperature') || 
                           device.capabilities.includes('Humidity') || 
                           device.capabilities.includes('Illuminance')) {
                    tile.classList.add('sensor');
                    applyTileColors(tile, false);
                }
                
                // Build tile content (icon and name only, controls added below)
                let content = `
                    <div class="tile-icon">` + getIcon(device) + `</div>
                    <div class="tile-name">` + device.name + `</div>
                `;
                
                tile.innerHTML = content;
                
                // Apply global icon size
                const iconElement = tile.querySelector('.tile-icon');
                if (customColors.iconSize) {
                    iconElement.style.fontSize = customColors.iconSize + 'em';
                }
                
                tile.dataset.deviceId = device.id;
                tile.dataset.row = row;
                tile.dataset.col = col;
                
                // Apply text alignment
                if (customColors.textAlign) {
                    tile.style.textAlign = customColors.textAlign;
                }
                
                // Add color picker if device supports color control
                if (device.capabilities.includes('ColorControl') && device.hue !== null && device.saturation !== null) {
                    // Determine current color: prefer colorTemperature if available, otherwise use HSV
                    let currentColor;
                    if (device.capabilities.includes('ColorTemperature') && device.colorTemperature !== null) {
                        // Device is in color temperature mode - show the kelvin color
                        currentColor = kelvinToHex(device.colorTemperature);
                    } else {
                        // Device is in color mode - show the HSV color
                        currentColor = hsvToHex(device.hue, device.saturation);
                    }
                    
                    // Create container for color picker
                    const pickerContainer = document.createElement('div');
                    pickerContainer.className = 'color-picker-container';
                    
                    // Create hidden color input
                    const colorInput = document.createElement('input');
                    colorInput.type = 'color';
                    colorInput.className = 'color-picker-input';
                    colorInput.value = currentColor;
                    colorInput.id = 'colorPicker' + device.id;
                    
                    // Create visible circular display
                    const colorDisplay = document.createElement('div');
                    colorDisplay.className = 'color-picker-display';
                    colorDisplay.style.backgroundColor = currentColor;
                    colorDisplay.title = 'Click to change color';
                    
                    // Click display to trigger hidden input
                    colorDisplay.addEventListener('click', (e) => {
                        e.stopPropagation();
                        colorInput.click();
                    });
                    
                    // Prevent refresh while color picker is open
                    colorInput.addEventListener('click', () => {
                        colorPickerOpen = true;
                        if (colorPickerTimeout) clearTimeout(colorPickerTimeout);
                        colorPickerTimeout = setTimeout(() => {
                            colorPickerOpen = false;
                        }, 60000);
                    });
                    
                    colorInput.addEventListener('focus', () => {
                        colorPickerOpen = true;
                    });
                    
                    colorInput.addEventListener('blur', () => {
                        colorPickerOpen = false;
                        if (colorPickerTimeout) clearTimeout(colorPickerTimeout);
                    });
                    
                    colorInput.addEventListener('change', (e) => {
                        setColor(device.id, e.target.value);
                        colorDisplay.style.backgroundColor = e.target.value;
                        colorPickerOpen = false;
                        if (colorPickerTimeout) clearTimeout(colorPickerTimeout);
                    });
                    
                    colorInput.addEventListener('cancel', () => {
                        colorPickerOpen = false;
                        if (colorPickerTimeout) clearTimeout(colorPickerTimeout);
                    });
                    
                    pickerContainer.appendChild(colorInput);
                    pickerContainer.appendChild(colorDisplay);
                    tile.appendChild(pickerContainer);
                }
                
                // Create container for labels (status and color temp on same row when both present)
                const labelsContainer = document.createElement('div');
                labelsContainer.style.marginTop = 'auto';
                labelsContainer.style.marginBottom = '5px';
                
                // Add color temperature label if device supports it
                let tempLabel = null;
                const hasColorTemp = device.capabilities.includes('ColorTemperature') && device.colorTemperature !== null;
                
                if (hasColorTemp) {
                    // When both status and color temp exist, use flexbox
                    labelsContainer.style.display = 'flex';
                    labelsContainer.style.justifyContent = 'space-between';
                    labelsContainer.style.alignItems = 'center';
                    
                    // Add status text (dimmer level on left)
                    const statusDiv = document.createElement('div');
                    statusDiv.className = 'tile-status';
                    statusDiv.textContent = device.statusText;
                    labelsContainer.appendChild(statusDiv);
                    
                    // Add color temperature label (on right)
                    tempLabel = document.createElement('div');
                    tempLabel.className = 'temp-label';
                    tempLabel.textContent = device.colorTemperature + 'K';
                    tempLabel.style.marginBottom = '0';
                    tempLabel.style.marginTop = '0';
                    labelsContainer.appendChild(tempLabel);
                } else {
                    // When only status text, let it inherit alignment from parent tile
                    const statusDiv = document.createElement('div');
                    statusDiv.className = 'tile-status';
                    statusDiv.textContent = device.statusText;
                    labelsContainer.appendChild(statusDiv);
                }
                
                tile.appendChild(labelsContainer);
                
                // Add color temperature slider if device supports it
                if (device.capabilities.includes('ColorTemperature') && device.colorTemperature !== null) {
                    const tempSlider = document.createElement('input');
                    tempSlider.type = 'range';
                    tempSlider.min = '2000';
                    tempSlider.max = '6500';
                    tempSlider.step = '100';
                    tempSlider.value = device.colorTemperature;
                    tempSlider.className = 'color-temp-slider';
                    tempSlider.style.marginTop = '5px';
                    tempSlider.title = 'Adjust color temperature';
                    
                    tempSlider.addEventListener('mousedown', () => {
                        sliderActive = true;
                        // Safety timeout - always clear sliderActive after 2 seconds
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    
                    tempSlider.addEventListener('touchstart', () => {
                        sliderActive = true;
                        // Safety timeout - always clear sliderActive after 2 seconds
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    
                    tempSlider.addEventListener('mouseup', () => {
                        sliderActive = false;
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    tempSlider.addEventListener('touchend', () => {
                        sliderActive = false;
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    // Update label as slider moves
                    tempSlider.addEventListener('input', (e) => {
                        if (programmaticUpdate) return;  // Ignore programmatic updates
                        if (tempLabel) {
                            tempLabel.textContent = e.target.value + 'K';
                        }
                    });
                    
                    // Send command when slider is released
                    tempSlider.addEventListener('change', (e) => {
                        if (programmaticUpdate) return;  // Ignore programmatic updates
                        e.stopPropagation();
                        setColorTemp(device.id, e.target.value);
                        sliderActive = false;
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    tile.appendChild(tempSlider);
                }
                
                // Add dimmer slider for dimmers (but not for window shades or inverted shades that will get their own slider)
                if (device.capabilities.includes('SwitchLevel') && device.level !== null && !device.capabilities.includes('WindowShade') && !device.invertShade) {
                    const slider = document.createElement('input');
                    slider.type = 'range';
                    slider.min = '0';
                    slider.max = '100';
                    slider.value = device.level;
                    slider.className = 'dimmer-slider';
                    slider.style.marginTop = '5px';
                    
                    slider.addEventListener('mousedown', () => { 
                        sliderActive = true; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    slider.addEventListener('touchstart', () => { 
                        sliderActive = true; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    slider.addEventListener('mouseup', () => { 
                        sliderActive = false; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    slider.addEventListener('touchend', () => { 
                        sliderActive = false; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    slider.addEventListener('change', (e) => {
                        if (programmaticUpdate) return;  // Ignore programmatic updates
                        e.stopPropagation();
                        setLevel(device.id, e.target.value);
                        sliderActive = false;
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    tile.appendChild(slider);
                }
                
                // Add position/level slider for window shades or inverted shades
                if ((device.capabilities.includes('WindowShade') || device.invertShade) && device.position !== null) {
                    const slider = document.createElement('input');
                    slider.type = 'range';
                    slider.min = '0';
                    slider.max = '100';
                    slider.value = device.position;
                    slider.className = 'dimmer-slider';
                    slider.style.marginTop = '5px';
                    slider.title = 'Adjust blind position (0=closed, 100=open)';
                    
                    slider.addEventListener('mousedown', () => { 
                        sliderActive = true; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    slider.addEventListener('touchstart', () => { 
                        sliderActive = true; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                        sliderActiveTimeout = setTimeout(() => { sliderActive = false; }, 2000);
                    });
                    slider.addEventListener('mouseup', () => { 
                        sliderActive = false; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    slider.addEventListener('touchend', () => { 
                        sliderActive = false; 
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    slider.addEventListener('change', (e) => {
                        if (programmaticUpdate) return;  // Ignore programmatic updates
                        e.stopPropagation();
                        // Use setPosition for true window shades, setLevel for inverted shades
                        if (device.capabilities.includes('WindowShade')) {
                            setPosition(device.id, e.target.value);
                        } else {
                            setLevel(device.id, e.target.value);
                        }
                        sliderActive = false;
                        if (sliderActiveTimeout) clearTimeout(sliderActiveTimeout);
                    });
                    
                    tile.appendChild(slider);
                }
                
                // Configure tile for edit mode or normal mode
                if (editMode) {
                    tile.draggable = true;
                    tile.classList.add('draggable');
                    tile.addEventListener('dragstart', (e) => handleDragStart(e, device));
                    tile.addEventListener('dragover', handleDragOver);
                    tile.addEventListener('drop', handleDrop);
                    tile.addEventListener('dragend', handleDragEnd);
                } else {
                    // THE KEY FEATURE: Entire tile is clickable!
                    // Pass device ID, not device object, so we get fresh state on each click
                    tile.addEventListener('click', (e) => handleTileClick(device.id, e));
                }
                
                // Cache tile element for incremental updates
                tileElements[device.id] = tile;
                
                container.appendChild(tile);
            }
        
        // Load custom colors
        async function loadCustomColors() {
            const url = API_BASE + '/api/getCustomColors?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            try {
                const response = await fetch(url);
                if (response.ok) {
                    const colors = await response.json();
                    if (colors && Object.keys(colors).length > 0) {
                        // Backward compatibility: convert old tileOff to tileOff1/tileOff2
                        if (colors.tileOff && !colors.tileOff1) {
                            colors.tileOff1 = colors.tileOff;
                            colors.tileOff2 = colors.tileOff;
                            delete colors.tileOff;
                        }
                        customColors = colors;
                        console.log('Applied custom colors:', customColors);
                        applyCustomColors();
                    } else {
                        console.log('No custom colors found, using defaults:', customColors);
                    }
                } else {
                    console.log('Failed to fetch colors, status:', response.status);
                }
            } catch (error) {
                console.log('No custom colors saved yet:', error);
            }
        }
        
        // Apply custom colors to page
        function applyCustomColors() {
            console.log('Applying colors:', customColors);
            document.body.style.background = customColors.background;
            document.getElementById('bgColor').value = customColors.background;
            document.getElementById('tileOffColor1').value = customColors.tileOff1;
            document.getElementById('tileOffColor2').value = customColors.tileOff2;
            document.getElementById('tileOnColor1').value = customColors.tileOn1;
            document.getElementById('tileOnColor2').value = customColors.tileOn2;
            if (customColors.textColor && customColors.textColor !== 'auto') {
                document.getElementById('textColor').value = customColors.textColor;
            } else {
                document.getElementById('textColor').value = '#ffffff';
            }
            if (customColors.iconSize) {
                document.getElementById('iconSize').value = customColors.iconSize;
                document.getElementById('iconSizeLabel').textContent = customColors.iconSize.toFixed(1) + 'em';
            }
            if (customColors.textAlign) {
                document.getElementById('textAlign').value = customColors.textAlign;
            } else {
                document.getElementById('textAlign').value = 'center';
            }
            
            console.log('Color pickers updated. Rendering dashboard...');
            // Update tile colors
            renderDashboard();
        }
        
        // Save custom colors
        async function saveCustomColors() {
            customColors.background = document.getElementById('bgColor').value;
            customColors.tileOff1 = document.getElementById('tileOffColor1').value;
            customColors.tileOff2 = document.getElementById('tileOffColor2').value;
            customColors.tileOn1 = document.getElementById('tileOnColor1').value;
            customColors.tileOn2 = document.getElementById('tileOnColor2').value;
            customColors.textColor = document.getElementById('textColor').value;
            customColors.iconSize = parseFloat(document.getElementById('iconSize').value);
            customColors.textAlign = document.getElementById('textAlign').value;
            
            console.log('Saving colors:', customColors);
            
            const url = API_BASE + '/api/saveCustomColors?access_token=' + encodeURIComponent(ACCESS_TOKEN);
            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(customColors)
                });
                if (response.ok) {
                    console.log('Colors saved successfully');
                    applyCustomColors();
                } else {
                    console.error('Failed to save colors, status:', response.status);
                }
            } catch (error) {
                console.error('Error saving colors:', error);
            }
        }
        

        
        // Reset color to default
        window.resetColor = function(type) {
            const defaults = {
                background: '${darkMode ? '#1a1a1a' : '#f5f5f5'}',
                tileOff1: '${darkMode ? '#2d2d2d' : '#ffffff'}',
                tileOff2: '${darkMode ? '#2d2d2d' : '#ffffff'}',
                tileOn1: '#667eea',
                tileOn2: '#764ba2',
                textColor: 'auto'
            };
            
            switch(type) {
                case 'background':
                    document.getElementById('bgColor').value = defaults.background;
                    break;
                case 'tileOff1':
                    document.getElementById('tileOffColor1').value = defaults.tileOff1;
                    break;
                case 'tileOff2':
                    document.getElementById('tileOffColor2').value = defaults.tileOff2;
                    break;
                case 'tileOn1':
                    document.getElementById('tileOnColor1').value = defaults.tileOn1;
                    break;
                case 'tileOn2':
                    document.getElementById('tileOnColor2').value = defaults.tileOn2;
                    break;
                case 'textColor':
                    customColors.textColor = 'auto';
                    document.getElementById('textColor').value = '#ffffff';
                    break;
            }
            saveCustomColors();
        };
        
        window.resetIconSize = function() {
            document.getElementById('iconSize').value = 2.5;
            document.getElementById('iconSizeLabel').textContent = '2.5em';
            saveCustomColors();
        };
        
        window.resetTextAlign = function() {
            document.getElementById('textAlign').value = 'center';
            saveCustomColors();
        };
        
        // Color input change handlers (add after DOM loaded)
        setTimeout(() => {
            document.getElementById('bgColor').addEventListener('change', saveCustomColors);
            document.getElementById('tileOffColor1').addEventListener('change', saveCustomColors);
            document.getElementById('tileOffColor2').addEventListener('change', saveCustomColors);
            document.getElementById('tileOnColor1').addEventListener('change', saveCustomColors);
            document.getElementById('tileOnColor2').addEventListener('change', saveCustomColors);
            document.getElementById('textColor').addEventListener('change', saveCustomColors);
            
            // Icon size slider
            const iconSizeSlider = document.getElementById('iconSize');
            const iconSizeLabel = document.getElementById('iconSizeLabel');
            iconSizeSlider.addEventListener('input', (e) => {
                iconSizeLabel.textContent = parseFloat(e.target.value).toFixed(1) + 'em';
            });
            iconSizeSlider.addEventListener('change', saveCustomColors);
            
            // Text alignment dropdown
            document.getElementById('textAlign').addEventListener('change', saveCustomColors);
        }, 100);
        
        // Initial load - load colors and grid layout first, then devices
        Promise.all([
            loadCustomColors(),
            loadGridLayout()
        ]).then(() => {
            console.log('Colors, icon sizes, and grid layout loaded, now fetching devices');
            return fetchDevices();
        }).then(() => {
            // Initialize grid layout if needed (for new devices)
            if (Object.keys(gridLayout).length === 0 || 
                devices.some(d => !gridLayout[d.id])) {
                initializeGridLayout();
            }
            renderDashboard();
            // Start adaptive polling
            restartPolling();
        }).catch((err) => {
            console.error('Error during initialization:', err);
            // Still try to load devices even if setup fails
            fetchDevices().then(() => {
                initializeGridLayout();
                renderDashboard();
                restartPolling();
            });
        });
        
        // Edit button handler
        document.getElementById('editButton').addEventListener('click', toggleEditMode);
    </script>
</body>
</html>
    """
    
    render contentType: "text/html", data: html
}

def getDevices() {
    def deviceList = []
    
    // Get saved layout order
    def savedOrder = state.deviceOrder ?: []
    
    // Build inverted device ID set once for better performance
    def invertedIds = (invertDevices?.collect { it.id.toString() } ?: []) as Set
    
    // Add switches
    switches?.each { device ->
        def caps = ["Switch"]
        def isInverted = invertedIds.contains(device.id.toString())
        def level = device.hasCapability("SwitchLevel") ? device.currentValue("level") : null
        def deviceData = [
            id: device.id,
            name: device.displayName,
            type: "switch",
            status: device.currentValue("switch"),
            statusText: device.currentValue("switch") == "on" ? "On" : "Off",
            invertShade: isInverted
        ]
        
        // Add position for inverted switches with level capability
        if (isInverted && level != null) {
            deviceData.position = level
            deviceData.level = level
            deviceData.statusText = level != null ? "${level}%" : deviceData.statusText
        }
        
        // Check for color control capability
        if (device.hasCapability("ColorControl")) {
            caps << "ColorControl"
            deviceData.hue = device.currentValue("hue")
            deviceData.saturation = device.currentValue("saturation")
        }
        
        // Check for color temperature capability
        if (device.hasCapability("ColorTemperature")) {
            caps << "ColorTemperature"
            deviceData.colorTemperature = device.currentValue("colorTemperature")
        }
        
        deviceData.capabilities = caps
        deviceList << deviceData
    }
    
    // Add dimmers
    dimmers?.each { device ->
        def level = device.currentValue("level")
        def caps = ["Switch", "SwitchLevel"]
        def isInverted = invertedIds.contains(device.id.toString())
        def deviceData = [
            id: device.id,
            name: device.displayName,
            type: "dimmer",
            status: device.currentValue("switch"),
            level: level,
            statusText: device.currentValue("switch") == "on" ? "On (${level}%)" : "Off",
            invertShade: isInverted
        ]
        
        // Add position for inverted dimmers
        if (isInverted) {
            deviceData.position = level
            deviceData.statusText = "${level}%"
        }
        
        // Check for color control capability
        if (device.hasCapability("ColorControl")) {
            caps << "ColorControl"
            deviceData.hue = device.currentValue("hue")
            deviceData.saturation = device.currentValue("saturation")
        }
        
        // Check for color temperature capability
        if (device.hasCapability("ColorTemperature")) {
            caps << "ColorTemperature"
            deviceData.colorTemperature = device.currentValue("colorTemperature")
        }
        
        deviceData.capabilities = caps
        deviceList << deviceData
    }
    
    // Add window shades
    shades?.each { device ->
        def position = device.currentValue("position")
        def shadeStatus = device.currentValue("windowShade") ?: "unknown"
        def isInverted = invertedIds.contains(device.id.toString())
        log.debug "Shade ${device.displayName} (${device.id}): position=${position}, isInverted=${isInverted}"
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "windowShade",
            status: shadeStatus,
            position: position,
            statusText: position != null ? "${position}% Open" : shadeStatus.capitalize(),
            capabilities: ["WindowShade"],
            invertShade: isInverted
        ]
    }
    
    // Add locks
    locks?.each { device ->
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "lock",
            status: device.currentValue("lock"),
            statusText: device.currentValue("lock") == "locked" ? "Locked" : "Unlocked",
            capabilities: ["Lock"]
        ]
    }
    
    // Add thermostats
    thermostats?.each { device ->
        def temp = device.currentValue("temperature")
        def mode = device.currentValue("thermostatMode")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "thermostat",
            status: mode,
            temperature: temp,
            statusText: "${temp}° - ${mode}",
            capabilities: ["Thermostat"]
        ]
    }
    
    // Add contact sensors
    contactSensors?.each { device ->
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "contact",
            status: device.currentValue("contact"),
            statusText: device.currentValue("contact") == "open" ? "Open" : "Closed",
            capabilities: ["ContactSensor"]
        ]
    }
    
    // Add motion sensors
    motionSensors?.each { device ->
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "motion",
            status: device.currentValue("motion"),
            statusText: device.currentValue("motion") == "active" ? "Motion" : "No Motion",
            capabilities: ["MotionSensor"]
        ]
    }
    
    // Add temperature sensors
    temperatureSensors?.each { device ->
        def temp = device.currentValue("temperature")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "temperature",
            status: "reading",
            temperature: temp,
            statusText: "${temp}°",
            capabilities: ["Temperature"]
        ]
    }
    
    // Add humidity sensors
    humiditySensors?.each { device ->
        def humidity = device.currentValue("humidity")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "humidity",
            status: "reading",
            humidity: humidity,
            statusText: "${humidity}%",
            capabilities: ["Humidity"]
        ]
    }
    
    // Add illuminance sensors
    illuminanceSensors?.each { device ->
        def lux = device.currentValue("illuminance")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "illuminance",
            status: "reading",
            illuminance: lux,
            statusText: "${lux} lux",
            capabilities: ["Illuminance"]
        ]
    }
    
    // Add water sensors
    waterSensors?.each { device ->
        def waterStatus = device.currentValue("water")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "water",
            status: waterStatus,
            statusText: waterStatus == "wet" ? "⚠️ Water Detected" : "Dry",
            capabilities: ["Water"]
        ]
    }
    
    // Add smoke detectors
    smokeSensors?.each { device ->
        def smokeStatus = device.currentValue("smoke")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "smoke",
            status: smokeStatus,
            statusText: smokeStatus == "detected" ? "⚠️ SMOKE!" : "Clear",
            capabilities: ["Smoke"]
        ]
    }
    
    // Add CO detectors
    coSensors?.each { device ->
        def coStatus = device.currentValue("carbonMonoxide")
        deviceList << [
            id: device.id,
            name: device.displayName,
            type: "carbonMonoxide",
            status: coStatus,
            statusText: coStatus == "detected" ? "⚠️ CO DETECTED!" : "Clear",
            capabilities: ["CarbonMonoxide"]
        ]
    }
    
    // Apply saved order
    if (savedOrder) {
        deviceList = deviceList.sort { a, b ->
            def aIndex = savedOrder.indexOf(a.id.toString())
            def bIndex = savedOrder.indexOf(b.id.toString())
            // Put devices not in saved order at the end
            if (aIndex == -1) aIndex = 999999
            if (bIndex == -1) bIndex = 999999
            return aIndex <=> bIndex
        }
    }
    
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson(deviceList)
}

def sendCommand() {
    def deviceId = params.id
    def command = params.command
    
    log.debug "sendCommand: deviceId=${deviceId}, command=${command}"
    
    def device = findDevice(deviceId)
    if (!device) {
        log.error "Device not found: ${deviceId}"
        render contentType: "application/json", status: 404, data: '{"error":"Device not found"}'
        return
    }
    
    try {
        log.debug "Executing ${command}() on ${device.displayName}"
        device."${command}"()
        render contentType: "application/json", data: '{"success":true}'
    } catch (e) {
        log.error "Error executing command ${command} on device ${deviceId}: ${e}"
        render contentType: "application/json", status: 500, data: "{\"error\":\"${e.message}\"}"
    }
}

def setLevel() {
    def deviceId = params.id
    def level = params.level as Integer
    
    def device = findDevice(deviceId)
    if (!device) {
        render contentType: "application/json", status: 404, data: '{"error":"Device not found"}'
        return
    }
    
    try {
        device.setLevel(level)
        render contentType: "application/json", data: '{"success":true}'
    } catch (e) {
        log.error "Error setting level: ${e}"
        render contentType: "application/json", status: 500, data: "{\"error\":\"${e.message}\"}"
    }
}

def setPosition() {
    def deviceId = params.id
    def position = params.position as Integer
    
    def device = findDevice(deviceId)
    if (!device) {
        render contentType: "application/json", status: 404, data: '{"error":"Device not found"}'
        return
    }
    
    try {
        device.setPosition(position)
        render contentType: "application/json", data: '{"success":true}'
    } catch (e) {
        log.error "Error setting position: ${e}"
        render contentType: "application/json", status: 500, data: "{\"error\":\"${e.message}\"}"
    }
}

def findDevice(deviceId) {
    def allDevices = []
    if (switches) allDevices.addAll(switches)
    if (dimmers) allDevices.addAll(dimmers)
    if (shades) allDevices.addAll(shades)
    if (locks) allDevices.addAll(locks)
    if (thermostats) allDevices.addAll(thermostats)
    if (contactSensors) allDevices.addAll(contactSensors)
    if (motionSensors) allDevices.addAll(motionSensors)
    if (temperatureSensors) allDevices.addAll(temperatureSensors)
    if (humiditySensors) allDevices.addAll(humiditySensors)
    if (illuminanceSensors) allDevices.addAll(illuminanceSensors)
    if (waterSensors) allDevices.addAll(waterSensors)
    if (smokeSensors) allDevices.addAll(smokeSensors)
    if (coSensors) allDevices.addAll(coSensors)
    
    log.debug "findDevice: Looking for deviceId=${deviceId} in ${allDevices.size()} devices"
    
    // Convert to Long for comparison
    Long targetId = Long.parseLong(deviceId.toString())
    
    def found = allDevices.find { device ->
        return device.id.toLong() == targetId
    }
    
    if (!found) {
        log.error "Device not found: ${deviceId}"
    }
    
    return found
}

def saveLayout() {
    def body = request.JSON
    def positions = body?.positions
    
    if (positions) {
        state.gridLayout = positions
        log.debug "Grid layout saved: ${positions}"
        render contentType: "application/json", data: '{"success":true}'
    } else {
        render contentType: "application/json", status: 400, data: '{"error":"No positions provided"}'
    }
}

def setColor() {
    def deviceId = params.id
    def body = request.JSON
    def hexColor = body?.hex
    
    log.debug "setColor called: deviceId=${deviceId}, hex=${hexColor}"
    
    def device = findDevice(deviceId)
    if (!device) {
        render contentType: "application/json", status: 404, data: '{"error":"Device not found"}'
        return
    }
    
    if (!hexColor || !hexColor.startsWith('#')) {
        render contentType: "application/json", status: 400, data: '{"error":"Invalid hex color"}'
        return
    }
    
    try {
        // Convert hex to HSV for Hubitat
        def rgb = hexToRgb(hexColor)
        def hsv = rgbToHsv(rgb.r, rgb.g, rgb.b)
        
        log.debug "Converted ${hexColor} to HSV: hue=${hsv.hue}, sat=${hsv.saturation}"
        
        // Set the color on the device
        device.setColor([hue: hsv.hue, saturation: hsv.saturation, level: 100])
        
        render contentType: "application/json", data: '{"success":true}'
    } catch (e) {
        log.error "Error setting color: ${e}"
        render contentType: "application/json", status: 500, data: "{\"error\":\"${e.message}\"}"
    }
}

def setColorTemp() {
    def deviceId = params.id
    def kelvin = params.kelvin as Integer
    
    log.debug "setColorTemp called: deviceId=${deviceId}, kelvin=${kelvin}"
    
    def device = findDevice(deviceId)
    if (!device) {
        render contentType: "application/json", status: 404, data: '{"error":"Device not found"}'
        return
    }
    
    if (kelvin < 2000 || kelvin > 6500) {
        render contentType: "application/json", status: 400, data: '{"error":"Invalid color temperature (2000-6500K)"}'
        return
    }
    
    try {
        device.setColorTemperature(kelvin)
        render contentType: "application/json", data: '{"success":true}'
    } catch (e) {
        log.error "Error setting color temperature: ${e}"
        render contentType: "application/json", status: 500, data: "{\"error\":\"${e.message}\"}"
    }
}

def hexToRgb(String hex) {
    hex = hex.replace('#', '')
    return [
        r: Integer.parseInt(hex.substring(0, 2), 16),
        g: Integer.parseInt(hex.substring(2, 4), 16),
        b: Integer.parseInt(hex.substring(4, 6), 16)
    ]
}

def rgbToHsv(int r, int g, int b) {
    def rPrime = r / 255.0
    def gPrime = g / 255.0
    def bPrime = b / 255.0
    
    def cMax = Math.max(rPrime, Math.max(gPrime, bPrime))
    def cMin = Math.min(rPrime, Math.min(gPrime, bPrime))
    def delta = cMax - cMin
    
    // Calculate hue
    def hue = 0
    if (delta != 0) {
        if (cMax == rPrime) {
            hue = 60 * (((gPrime - bPrime) / delta) % 6)
        } else if (cMax == gPrime) {
            hue = 60 * (((bPrime - rPrime) / delta) + 2)
        } else {
            hue = 60 * (((rPrime - gPrime) / delta) + 4)
        }
    }
    
    if (hue < 0) hue += 360
    
    // Calculate saturation
    def saturation = (cMax == 0) ? 0 : (delta / cMax) * 100
    
    // Convert hue from 0-360 to 0-100 for Hubitat
    hue = (hue / 360.0) * 100.0
    
    return [
        hue: Math.round(hue),
        saturation: Math.round(saturation)
    ]
}

def getCustomColors() {
    def colors = state.customColors ?: [:]
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson(colors)
}

def getGridLayout() {
    def layout = state.gridLayout ?: [:]
    render contentType: "application/json", data: groovy.json.JsonOutput.toJson(layout)
}

def saveCustomColors() {
    def body = request.JSON
    if (body) {
        state.customColors = body
        log.debug "Custom colors saved: ${body}"
        render contentType: "application/json", data: '{"success":true}'
    } else {
        render contentType: "application/json", status: 400, data: '{"error":"No colors provided"}'
    }
}
