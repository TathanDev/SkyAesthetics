# Sky Box Settings

This Settings allow you to add a fully custom sky box to the world.

If no sun settings are specified, the world won't have a sun.



```json
{
   //other sky settings ...
  "sky_box": {
    "gradation": 100,
    "texture": "sky_aesthetics:textures/skyboxes/venus.png",
    "rotation": [0.0, 0.0, 0.0],
    "dynamic_rotation": {
      "axis": "XP",
      "rotation": "DAY"
    }
  }
}
```

## Fields
- `gradation`: The gradation is the quality of the skybox sphere. More gradation equals to a better sphere but worst performance.
- `texture`: The texture of the skybox.
- `rotation`: A three float arrays to modify the rotation of the skybox.
- `dynamic_rotation`: An object that contains settings for the skybox rotation.

## Dynamic Rotation Settings

you can move the skybox with the time !

```json
{
  "dynamic_rotation": {
    "axis": "XP",
    "rotation": "DAY"
  }
}
```

- `axis`: the axis of the rotation. Possible values : `XP`, `YP` and `ZP`
- `rotation`: The rotation the skybox will follow (like the sun or the moon). Possible values : `DAY`, `NIGHT` or `STATIC`

<style>

video {
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 2px 2px 12px rgba(0, 0, 0, 0.1);
}

</style>