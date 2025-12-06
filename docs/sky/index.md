# Creating Your first Sky

When you create a new Sky project, it is important to understand the file structure that organizes your sky.
Here the require field structure for a Sky Aesthetics resource pack:

## Basic File

```json
{
  "world": "minecraft:overworld",
  "id": "minecraft:default",
  "weather": true,
  "sky_objects": []
}
```

Let's break down each field:
- `world`: Specifies the dimension where this sky will be applied. Common values include `minecraft:overworld`, `minecraft:the_nether`, and `minecraft:the_end`.
- `id`: A unique identifier for your sky configuration. This helps differentiate between multiple sky setups.
- `weather`: A boolean value (`true` or `false`) that determines whether the rain and snow effects are displayed in this sky.
- `sky_objects`: An array that will contain all the sky objects (like moons, planets, etc.) you want to add to your sky. You can learn more about sky objects [here](./objects.md).

This file should create a sky similar to the vanilla one.
To actually change the sky, you need to add **Settings** to your sky configuration file.

## Adding Sky Settings
To customize your sky, you can add settings to your file.

Here is an example of a sky configuration file with some settings:
```json
{
  "world": "minecraft:overworld",
  "id": "minecraft:custom_sky",
  "weather": false,
  "sky_objects": [],
  "cloud_settings": {
    "cloud": true,
    "cloud_height": 300
  }
}
```

To add a setting, you need to add a new field named like the settings id to your sky configuration file.
Then you will be able to config these settings.

