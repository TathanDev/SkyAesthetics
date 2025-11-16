# Stars Settings

This Settings allow you to customize the stars in the sky.

```json
{
   //other sky settings ...
  "stars": {
    "scale": 0.05,
    "color": [
      255,
      255,
      255
    ],
    "vanilla": true,
    "moving_stars": false,
    "count": 100000,
    "all_days_visible": false
  }
}
```

## Fields
- `vanilla`: A boolean value (`true` or `false`) that determines whether to use the vanilla stars. If set to true, all the other star settings will be ignored.
- `scale`: A float value to define the scale/size of the stars.
- `color`: An array of three integer values (ranging from 0 to 255) that define the RGB color of the stars. For example, `[255, 255, 255]` represents white color.
- `moving_stars`: A boolean value (`true` or `false`) that determines whether the stars should move slowly across the sky.
- `count`: An integer value that sets the number of stars to be displayed in the sky. The default value is `15000`. You can increase or decrease this value to change the density of stars.
- `all_days_visible`: A boolean value (`true` or `false`) that determines whether the stars are visible during the day as well as at night.
- `shooting_stars`: An object that contains settings for shooting stars.

## Shooting Stars Settings

Create beautiful shooting stars in your sky.

```json
{
   //other sky settings ...
  "stars": {
    //star settings ...
    "shooting_stars": {
      "percentage": 99,
      "random_lifetime": [20, 40],
      "color": [255, 255, 255],
      "scale": 0.1,
      "speed": 1,
      "rotation": 90
    },
    "all_days_visible": false
  }
}
```

### Fields
- `percentage`: An integer value (0-100) that defines the chance of a shooting star appearing each tick. A higher value increases the likelihood of shooting stars appearing.
- `random_lifetime`: An array of two integer values that define the minimum and maximum lifetime (in ticks) of a shooting star. The lifetime will be a random value between these two numbers.
- `color`: An array of three integer values (ranging from 0 to 255) that define the RGB color of the shooting stars.
- `scale`: A float value to define the scale/size of the shooting stars.
- `speed`: A float value that determines how fast the shooting stars move across the sky.
- `rotation`: A float value that sets the angle (in degrees) at which the shooting stars travel across the sky. 0 degrees make it random.