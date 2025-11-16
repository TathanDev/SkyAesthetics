# Render Condition Settings

This Setting allow you to configure when your sky will be visible.

This Setting as two variant
```json
{
   //other sky settings ...
  "condition": {
    "biomes": "#minecraft:is_cold"
  }
}
```

or

```json
{
  //other sky settings ...
  "condition": {
    "biome": "minecraft:badland"
  }
}

```

## Fields
- `biome`: A Resourcekey for a biome.
- `biomes`: A biome tag

