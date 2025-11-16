# Fog Settings

This Setting allow you to customize the fog in your sky.

```json
{
   //other sky settings ...
  "fog_settings": {
    "fog": true,
    "fog_color": [200, 200, 255],
    "fog_density": [20, 100]
  }
}
```

## Fields
- `fog`: A boolean value that define if there is fog or no.
- `fog_color` (**Opt**): An array of three integer values (ranging from 0 to 255) that define the RGB color of the fog.
- `fog_density`  (**Opt**): An array of two float. The first value is how many block there is between the player and the start of the fog. The second value is how many block there is between the player and when the fog is fully dense.

