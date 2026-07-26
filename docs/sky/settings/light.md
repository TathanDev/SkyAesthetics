# Light Settings <Badge type="warning" text="beta" />

This Setting allow you to configure how light works in the dimension when the sky is on.

```json5
{
   //other sky settings ...
  "light_settings": {
    "forceBrightLightmap": true,
    "constantAmbientLight": false
  }
}
```

## Fields
- `forceBrightLightmap`: A Boolean value that define if the Brigh Lightmap should be used (Idk why you would you use it). Optional, defaults to `false`.
- `constantAmbientLight`: A Boolean value that define if the world should have a constant ambient light (like in the end). Optional, defaults to `false`.