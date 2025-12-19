# Sky Objects
Sky Aesthetics allows you to add custom objects to the sky, such as moons, planets, and other celestial bodies.

Every Sky Objects you want to add need to be added to the `sky_objects` array in your sky configuration file.

```json
{
  //other sky settings ...
  "sky_objects": [
    {
      "rotation": [
        0.0,
        0.0,
        0.0
      ],
      "object_rotation": [
        0.0,
        0.0,
        0.0
      ],
      "height": 100,
      "rotation_type": "STATIC",
      "texture": "test:textures/sky/milky_way.png",
      "blend": true,
      "size": 100.0
    },
    // You can add more sky objects here
  ]
}
```

## Fields
- `rotation`: A three float arrays to modify the rotation of the sky object. This is the position of the object in the sky.
- `object_rotation`: A three float arrays to modify the rotation of the sky object itself. This is the rotation of the object around its own axis.
- `height`: An float value that sets the height of the sky object in the sky.
- `rotation_type`: The type of rotation for the sky object. It can be either `STATIC`, `DAY` or `NIGHT`.
  - `STATIC`: The object remains in a fixed position in the sky.
  - `DAY`: The object rotates with the sun during the day.
  - `NIGHT`: The object rotates with the moon during the night.
- `texture`: The Identifier of the sky object texture.
- `blend`: A boolean value (`true` or `false`) that determines whether the sky object should be blended with the sky.
- `size`: A float value to define the scale/size of the sky object.
