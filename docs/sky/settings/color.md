# Sky Color Settings

This Settings allow you to customize the sky color and the sunset color.

```json
{
   //other sky settings ...
  "sky_color": {
    "sunset_alpha_modifier": 1,
    "sky_color": [255, 255, 255, 1],
    "sunset_color": [255, 100, 100]
  }
}
```

## Fields

If no fields are specified, the default sky and sunset colors will be used. But if the settings is not present, the sky won't have sunset color.

- `sky_color` (**Opt**): An array of four values that define the RGBA color of the sky. The first three values represent the RGB color (ranging from 0 to 255), and the fourth value represents the alpha (opacity) ranging from 0.0 (fully transparent) to 1.0 (fully opaque).
- `sunset_color` (**Opt**): An array of three integer values (ranging from 0 to 255) that define the RGB color of the sunset.
- `sunset_alpha_modifier` (**Opt**): A float value that modifies the alpha (opacity) of the sunset color. The vanilla alpha value will be multiplied by this modifier.