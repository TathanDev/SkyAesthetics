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

# Changes

- Remove `fog` settings. You can now use `fog_settings` to enable fog.