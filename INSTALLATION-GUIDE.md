# Custom Dashboard App - Installation Guide

## Overview

This custom dashboard app solves your original problem: **the entire tile is clickable!** No need to precisely click buttons – just tap anywhere on a device tile to control it.

## Features

✅ **Entire tiles are clickable** - Click anywhere on a tile to toggle devices  
✅ Modern, responsive design  
✅ Support for switches, dimmers, locks, thermostats, and sensors  
✅ Dark mode support  
✅ Real-time device status updates  
✅ Dimmer sliders built into tiles  
✅ Works on desktop, tablet, and mobile  
✅ Local and cloud access  

## Installation Steps

### 1. Add the App Code

1. Log into your Hubitat hub web interface
2. Click **Developer Tools** > **Apps Code** in the left sidebar
3. Click **+ New App** button (top right)
4. Copy all the code from `custom-dashboard-app.groovy`
5. Paste it into the editor
6. Click **Save** button

### 2. Enable OAuth (REQUIRED!)

OAuth must be enabled for the dashboard to work:

1. While still in the Apps Code editor, click **OAuth** button (top right)
2. Click **Enable OAuth in App**
3. Click **Update** button
4. Click **Save** again to save the app

**⚠️ Important: The dashboard will not work without OAuth enabled!**

### 3. Install the App

1. Go to **Apps** in the left sidebar
2. Click **Add User App** button
3. Find "Custom Dashboard" in the list
4. Click to install it

### 4. Configure Your Dashboard

1. Give your dashboard a name (e.g., "Living Room Dashboard")
2. Select devices to include:
   - Switches/Lights
   - Dimmers
   - Locks
   - Thermostats
   - Contact Sensors
   - Motion Sensors
3. Configure settings:
   - **Auto-refresh interval**: How often to update (default: 5 seconds)
   - **Tile size**: Small, Medium, or Large
   - **Dark mode**: Enable/disable dark theme
4. Click **Done**

### 5. Access Your Dashboard

After saving, the app will display two URLs:

**Local URL** (for use within your home network):
```
https://[your-hub-ip]/apps/api/[app-id]/dashboard?access_token=[token]
```

**Cloud URL** (for remote access):
```
https://cloud.hubitat.com/api/[hub-uid]/apps/[app-id]/dashboard?access_token=[token]
```

Copy either URL and:
- Bookmark it in your browser
- Add it to your phone's home screen
- Use it in a tablet wall mount

## Usage

### Controlling Devices

**Switches/Lights:**
- Click anywhere on the tile to toggle on/off
- Tiles light up with a purple gradient when on

**Dimmers:**
- Click the tile to toggle on/off
- Use the slider to adjust brightness (0-100%)
- Slider is at the bottom of the tile

**Locks:**
- Click anywhere on the tile to lock/unlock
- Locked tiles show pink/red gradient
- Unlocked tiles show blue gradient

**Sensors (Contact/Motion):**
- Status-only tiles (read-only)
- Show current state with color coding
- Update automatically

**Thermostats:**
- Display current temperature and mode
- Status-only in this version

### Visual Feedback

- **Hover effect**: Tiles lift up slightly when you hover
- **Active state**: Tiles change color based on their state
- **Smooth animations**: All interactions are smooth and responsive
- **Status text**: Shows current state at bottom of each tile

## Tips & Tricks

### Creating Multiple Dashboards

You can install the app multiple times to create different dashboards:
- Kitchen Dashboard
- Bedroom Dashboard
- Security Dashboard (locks and sensors)
- Lighting Dashboard

Each instance is independent with its own URL.

### Mobile Home Screen

**iOS (iPhone/iPad):**
1. Open the dashboard URL in Safari
2. Tap the Share button
3. Select "Add to Home Screen"
4. Name it and tap "Add"

**Android:**
1. Open the dashboard URL in Chrome
2. Tap the menu (⋮)
3. Select "Add to Home Screen"
4. Name it and tap "Add"

### Tablet Wall Mount

Perfect for a tablet mounted on the wall:
1. Use the local URL for faster response
2. Set auto-refresh to 3-5 seconds
3. Choose "Large" tile size
4. Enable dark mode to save battery
5. Use a kiosk browser app to hide browser controls

### Troubleshooting

**Dashboard shows "Loading devices..." forever:**
- Check that OAuth is enabled in the app code
- Verify you're using the correct URL with access token
- Check if devices are selected in app settings

**Devices don't respond to clicks:**
- Ensure devices are properly selected in app settings
- Check device status in Hubitat's main Devices page
- Look at app logs: Apps > Custom Dashboard > View Logs

**Dashboard doesn't update:**
- Check your refresh interval setting
- Try manually refreshing your browser
- Verify network connectivity

**Can't access cloud URL:**
- Ensure your hub is registered with Hubitat cloud
- Check that cloud access is enabled in hub settings
- Verify the access token is in the URL

## Customization

### Change Colors

Edit the CSS in the app code to customize colors:
- Search for `background: linear-gradient` to find color definitions
- Modify hex color codes to your preference
- Save and refresh your dashboard

### Adjust Tile Sizes

In app settings, choose from:
- **Small**: 150px tiles (fits more on screen)
- **Medium**: 200px tiles (default, good balance)
- **Large**: 250px tiles (easier to tap, better for wall mounts)

### Modify Refresh Rate

Adjust the refresh interval:
- Faster (2-3 sec): More responsive, more hub load
- Slower (10-15 sec): Less hub load, saves bandwidth
- Default (5 sec): Good balance

## Security Notes

- URLs contain access tokens - treat them like passwords
- Only share dashboard URLs with trusted family members
- Use local URLs when possible (more secure, faster)
- Cloud URLs are encrypted via HTTPS
- You can regenerate access tokens by removing and reinstalling the app

## Comparison to Built-in Dashboards

| Feature | Easy Dashboard | Old Dashboard | Custom Dashboard |
|---------|---------------|---------------|------------------|
| Entire tile clickable | ❌ | ❌ (without CSS) | ✅ |
| Custom CSS | ❌ | ✅ | ✅ (built-in) |
| Modern UI | ✅ | ⚠️ | ✅ |
| Easy setup | ✅ | ⚠️ | ✅ |
| OAuth setup | ❌ | ❌ | Required |
| Full customization | ❌ | ⚠️ | ✅ |

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review app logs in Hubitat
3. Visit the Hubitat Community forums
4. Review the code comments in the .groovy file

## What's Next?

Possible enhancements you could add:
- Temperature controls for thermostats
- Color picker for RGB bulbs
- Scenes/routines buttons
- Weather display
- Time/date widget
- Custom tile icons
- Drag-and-drop tile reordering

The code is fully commented and ready for you to customize!

---

**Enjoy your new fully-clickable dashboard! 🎉**
