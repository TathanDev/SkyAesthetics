# Sky Aesthetics 1.3.0

## Features
- Custom Cloud Color
- Sky Object Rotation

## Breaking Changes
- Change the color of the stars to a Vector3
- Change the color of the constellation to a Vector3
```json
{
  "color": [160, 180, -1]
}
```

- Change the color of the sky Color to a Vector4f (R, G, B, and the alpha)
```json
{
  "color": [160, 180, 200, 1]
}
```

## Fix
- Fix sunrise color rendering ar midnigh ( #14 )
- Fix wrong sunrise color  ( #15 )