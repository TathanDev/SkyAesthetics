# Sky Aesthetics 2.1.3 for MC 26.1.x changelog

## Changes
- Move some logic to client side

## Fixes
- Fixed a GPU memory leak that could crash the game for skies using custom sky objects
- Fixed conditional skies, clouds, and fog not working on multiplayer servers
- Fixed a crash when the mod was installed on a dedicated NeoForge server
- Fixed custom sun/moon textures rendering as a thin sliver at the wrong size
- Fixed custom sun/moon not blending transparency and not fading in rain or at dawn/dusk
- Fixed a crash when clicking Duplicate in the editor with an empty or invalid id/dimension
- Fixed which sky is chosen when several skies target the same dimension (now deterministic)
- Fixed the "moving stars" option having no effect
- Fixed custom stars disappearing when the star count was between 1 and 99
- Fixed STATIC sky objects still drifting with the day/night cycle
- Custom cloud recolor/reposition now respects the "disable custom clouds" config option
- Custom skies now render in dimensions with no vanilla sky (e.g. the Nether)
- Fixed the editor silently dropping enabled-but-empty sections; export now warns instead
- Fixed the live preview sky leaking into the next world after leaving one
- Fixed a possible crash from resource reloads freeing sky graphics mid-frame
- Fixed a render pipeline registration race on NeoForge
- Reduced per-frame GPU work for the custom sun and moon