# Render Condition Settings

This Setting allow you to configure when your sky will be visible.

This Setting as two variant
```json
{
   //other sky settings ...
  "condition": {
    "biomes": "#minecraft:is_cold",
    "height_range": [10, 100]
  }
}
```

or

```json
{
  //other sky settings ...
  "condition": {
    "biome": "minecraft:badland",
    "height_range": [10, 100]

  }
}

```

## Fields
- `biome`: A Resourcekey for a biome.
- `biomes`: A biome tag
- `height_range`: An array of two integer. The player need to be in this height range for the sky to be rendered. (Opt)


