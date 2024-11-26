# Sky Aesthetics 1.4.0

## Features

- Added Fog Settings
  - Custom fog color
  - Custom fog density

```json
{
  "fog_settings": {
    "fog": true,
    "fog_color": [255, 0, 0],
    "fog_density": [10, 11]
  }
}
```

- Added cloud settings
  - Custom cloud color depending on the weather
  - Cloud Height
  - Render clouds
  
```json
{
  "cloud_settings": {
    "cloud": false,
    "cloud_height": 192,
    "cloud_color": {
      "base_color": [255, 0, 0],
      "rain_color": [0, 255, 0],
      "storm_color": [0, 0, 255],
      "always_base_color": false
    }
  }
}
```

# Changes

- Remove `fog` settings. You can now use `fog_settings` to enable fog.
- Move `clouds` and `clouds_height` to `cloud_settings` to enable clouds.