# Getting Started

Ok, let's get you started with your new sky !

## Installation
1. Download and install [Fabric](https://fabricmc.net/use/) or [Neoforged](https://neoforged.net).
2. Download [Sky Aesthetics](https://modrinth.com/mod/sky-aesthetics) mod.
3. (Optional) Download [OwO Lib](https://modrinth.com/mod/owo-lib) mod for extra features.

::: tip
You should use a minecraft launcher like the [Modrinth Launcher](https://modrinth.com/launcher) or [MultiMC](https://multimc.org/) to easily manage your mods and resource packs.
:::

## Resource Packs
Before started, you need to be confortable with creating resource packs. If you don't know how to create one, you can check [this tutorial](https://docs.neoforged.net/docs/resources/#assets).

### Terminology
Here are some terms you will often see in the documentation:
- **Namespace**: The name of your project. Minecraft's one is `minecraft` that's why every asset are located in the `assets/minecraft/*` folder.
- **Sky Object**: An object that can be added to the sky (like moons, planets, etc.)
- **Skybox**: A image that cover all the minecraft sky.

### File Structure
This is a basic structure of a resource pack using Sky Aesthetics.:
::: warning
Here [namespace] should be replaced by your resource pack namespace. If my datapack is named `my_cool_pack`, the path to my sky configuration file will be `assets/my_cool_pack/sky_aesthetics/your_sky.json`.
:::
```
├─ assets
│  └─ [namespace]
│         └─ sky_aesthetics
│            └─ your_sky.json
│         └─ textures
│            └─ [all your textures here]
└─ pack.mcmeta
```

### Creation Options
You can create your sky configuration file by hand or use the sky creation screen.
This screen is available in-game by pressing the button in the pause menu.

::: warning
The sky creation screen is only available when [OwO Lib](https://modrinth.com/mod/owo-lib) is installed.
:::
