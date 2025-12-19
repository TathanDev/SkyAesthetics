# Moon Settings

This Settings allow you to customize the moon in your sky.

If no sun settings are specified, the world won't have a moon.

```json
{
   //other sky settings ...
  "moon": {
    "texture": "minecraft:textures/environment/sun.png",
    "moon_phases": false,
    "size": 100,
    "height": 50
  }
}
```

## Fields
- `texture`: The Identifier of the sun texture.
- `moon_phases`: A boolean value (`true` or `false`) that determines whether the moon should change depending on the current moon phase.
- `size`: A float value to define the scale/size of the sun.
- `height`: An float value that sets the height of the sun in the sky.

