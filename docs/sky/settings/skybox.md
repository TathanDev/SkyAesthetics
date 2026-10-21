# Sky Box Settings

A sky can draw any number of skybox **layers** with `sky_boxes`. Each layer is a panorama or a cubemap with its
own visibility over the day, rotation, colour and draw order, so a day image and a night image are just two layers
that fade into each other.

The older single [`sky_box`](#legacy-sky-box) field still works and can be used next to `sky_boxes`.

```json
{
  //other sky settings ...
  "sky_boxes": [
    {
      "texture": "mypack:textures/skybox/day.png",
      "visible": "day",
      "tint_with_sky": true,
      "rain_opacity": 0.3
    },
    {
      "texture": "mypack:textures/skybox/night.png",
      "visible": "night",
      "fade": 1500,
      "rotation": { "source": "sun", "axis": "sun", "tilt": 35 }
    },
    {
      "texture": "mypack:textures/skybox/aurora.png",
      "blend": "add",
      "slot": "over_stars",
      "scroll": [0.0002, 0.0],
      "hemisphere": "upper",
      "horizon_fade": 15,
      "opacity": [
        { "time": 13000, "value": 0.0 },
        { "time": 15000, "value": 0.8 },
        { "time": 21000, "value": 0.8 },
        { "time": 23000, "value": 0.0 }
      ],
      "condition": { "biomes": "#minecraft:is_snowy" }
    }
  ],
  "transition_ticks": 40
}
```

Every field except `texture` is optional.

## Texture

`texture` is either one panorama or six cube faces.

- **Panorama**: an equirectangular image (2:1). The top of the image is the zenith, the middle row is the horizon,
  and the centre of the image faces north, with east to the right.
- **Cubemap**: an object with `up`, `down`, `north`, `south`, `east` and `west`. Each face is drawn as seen from
  inside the sky: side faces upright, `up` and `down` with the top edge of the image pointing north.

```json
"texture": {
  "up": "mypack:textures/skybox/up.png",
  "down": "mypack:textures/skybox/down.png",
  "north": "mypack:textures/skybox/north.png",
  "south": "mypack:textures/skybox/south.png",
  "east": "mypack:textures/skybox/east.png",
  "west": "mypack:textures/skybox/west.png"
}
```

::: tip Where to put the image
An id ending in `.png` (like `mypack:textures/skybox/day.png`) is loaded as its own texture. This is the
recommended way: large skyboxes don't bloat the shared celestial atlas, and `scroll` and `animation` only work this
way. Any other id (like `mypack:day`) is a sprite of the celestial atlas, i.e.
`assets/mypack/textures/environment/celestial/day.png`.
:::

## Visibility over the day

Time is the dimension's clock in ticks, the same value `/time set` uses: 0 is sunrise, 6000 noon, 12000 sunset,
18000 midnight.

| Field | Default | Description |
| --- | --- | --- |
| `visible` | `always` | A preset: `always`, `day`, `night`, `sunrise`, `sunset`. |
| `fade` | `1500` | Length in ticks of a preset's transitions. `day` is fully visible until 12000, fades out by 12000 + `fade`, and fades back in over the last `fade` ticks before 24000. `sunset` and `sunrise` peak in the middle of those fades. |
| `opacity` | | Custom keyframes `{ "time": ticks, "value": 0-1 }`, interpolated linearly and wrapping around midnight. Use either `visible` or `opacity`, not both. |
| `rain_opacity` | | Opacity multiplier while it fully rains (fades with the rain). |
| `thunder_opacity` | | Opacity multiplier during a full thunderstorm. |
| `moon_phases` | | Only visible on these moon phases, `0` (full moon) to `7`. |
| `follow_star_brightness` | `false` | Fades exactly like the vanilla stars. |
| `height_fade` | | `[from, to]`: fades in as the player climbs from `from` to `to` (reverse the two to fade out instead). |
| `condition` | | Only visible while the condition holds; see below. |
| `condition_fade` | `20` | Ticks to fade in or out when the condition changes. |

### Condition

Every field you set must hold.

| Field | Description |
| --- | --- |
| `biome` | A biome id. |
| `biomes` | A biome tag, e.g. `#minecraft:is_snowy`. |
| `height_range` | `[min, max]` player height. |
| `weather` | `clear`, `rain` or `thunder`. |
| `effects` | A list of effect ids; the layer shows while the player has any of them. |

## Look

| Field | Default | Description |
| --- | --- | --- |
| `gradation` | `32` | Sphere quality for panoramas (4-256). |
| `color` | | RGB tint, `[r, g, b]` from 0 to 255. |
| `brightness` | `1.0` | Multiplies the colour. |
| `tint_with_sky` | `false` | Multiplies the layer by the current vanilla sky colour, so sunsets and rain tint it. |
| `blend` | `alpha` | `alpha`, `add` (glows: nebulae, auroras), `multiply` (darkens: haze), `screen` (brightens), or `opaque`. |
| `scroll` | `[0, 0]` | UV units per tick, for drifting clouds or auroras (standalone panoramas only). |
| `animation` | | `{ "frames": 8, "frametime": 2 }`: the image is a vertical strip of frames (standalone textures only). |
| `hemisphere` | `full` | `upper` hides everything below the horizon, even while the layer rotates. |
| `horizon_fade` | `0` | With `upper`: height in degrees over which the layer fades out towards the horizon. |
| `fog_fade` | `false` | Fades the layer into the sky fog the same way the vanilla sky is. |

## Rotation

```json
"rotation": {
  "source": "sun",
  "speed": 1.0,
  "offset": 0.0,
  "axis": "sun",
  "tilt": 0.0,
  "orientation": [0.0, 0.0, 0.0]
}
```

| Field | Default | Description |
| --- | --- | --- |
| `source` | `sun` | What drives the angle: `sun`, `moon`, `stars` (the vanilla angles), `day_time` (the clock, one turn per day), `game_time` (spins forever), `static`. |
| `speed` | `1.0` | Multiplier on the source. Negative turns the other way. With `game_time` it is degrees per tick. |
| `offset` | `0.0` | Degrees added to the angle. |
| `axis` | `sun` | `sun` (the axis the sun turns around, so a painted sun stays on the real one), `XP`, `YP`, `ZP`, or `[x, y, z]`. |
| `tilt` | `0` | Tips the spin axis: a number turns it around the east-west axis (like latitude, for a fixed pole star); `[x, y, z]` is a full rotation. |
| `orientation` | `[0, 0, 0]` | Turns the image itself (e.g. to hide the seam) without moving the spin axis. |

## Draw order

| Field | Default | Description |
| --- | --- | --- |
| `slot` | `sky` | `background` (before the vanilla sky colour), `sky` (after it, before the sunset glow), `over_stars` (after the stars, before the sun and moon), `foreground` (after everything). |
| `hide` | `[]` | Vanilla parts to fade out while this layer is visible: `sky_disc`, `sunrise`, `stars`, `sun`, `moon`, `sky_objects`. |

Layers draw in list order inside their slot. A `background` layer needs `"hide": ["sky_disc"]` to be seen above the
horizon, since the vanilla sky colour is drawn over it.

## Changing skies

`transition_ticks` (on the sky, next to `sky_boxes`) crossfades skybox layers when this sky replaces another one,
for example when a biome or height `condition` switches skies. The previous sky's layers fade out over that many
ticks; other parts (stars, colours, sun) switch at once. For a biome-specific layer, a layer `condition` usually
looks better, since it fades on its own.

## Legacy sky box

The original single skybox. It keeps working exactly as before.

```json
{
   //other sky settings ...
  "sky_box": {
    "gradation": 100,
    "texture": "sky_aesthetics:textures/skyboxes/venus.png",
    "rotation": [0.0, 0.0, 0.0],
    "dynamic_rotation": {
      "axis": "SUN",
      "rotation": "DAY"
    }
  }
}
```

- `gradation`: The quality of the skybox sphere. More gradation means a rounder sphere but worse performance.
- `texture`: The texture of the skybox.
- `rotation`: Three floats to rotate the skybox.
- `dynamic_rotation`: Moves the skybox with the time.
  - `axis`: `SUN`, `XP`, `YP` or `ZP`. Use `SUN` to make a `DAY` skybox follow the sun exactly; the other axes turn
    around a different axis than the sun does.
  - `rotation`: `DAY`, `NIGHT` or `STATIC`.

<style>

video {
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 2px 2px 12px rgba(0, 0, 0, 0.1);
}

</style>
