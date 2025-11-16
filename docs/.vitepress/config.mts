import { defineConfig } from 'vitepress'

console.log("VitePress config loaded");

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Sky Aesthetics Documentation",
  description: "Craft beautiful skies for Minecraft with Sky Aesthetics mod.",
  base: '/SkyAesthetics/',
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Docs', link: '/sky' },
      { text: 'Download',
          items: [
              { text: 'Modrinth', link: 'https://modrinth.com/mod/sky-aesthetics' },
              { text: 'Curseforge', link: 'https://curseforge.com/minecraft/mc-mods/sky-aesthetics' }
          ]
      }
    ],

    sidebar: [
      {
        text: 'Introduction',
        collapsed: false,
        items: [
          { text: 'What is Sky Aesthetics', link: '/introduction/what-is-it' },
          { text: 'Getting Started', link: '/introduction/getting-started' }
        ]
      },
      {
          text: 'Creating a Sky',
          collapsed: false,
          items: [
              { text: 'File Structure', link: '/sky' },
              { text: 'Sky Object', link: '/sky/objects' },
              {
                  text: 'Settings',
                  collapsed: false,
                  items: [
                      { text: 'Sun Settings', link: '/sky/settings/sun' },
                      { text: 'Moon Settings', link: '/sky/settings/moon' },
                      { text: 'Sky Color Settings', link: '/sky/settings/color' },
                      { text: 'Cloud Settings', link: '/sky/settings/clouds' },
                      { text: 'Stars Settings', link: '/sky/settings/stars' },
                      { text: 'Skybox Settings', link: '/sky/settings/skybox' },
                      { text: 'Fog Settings', link: '/sky/settings/fog' }
                  ]
              }
            ]
        }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/vuejs/vitepress' },
      { icon: 'discord', link: 'https://discord.gg/d3GJSPbWbM' }

    ],
    search: {
        provider: 'local'
    },
    editLink: {
        pattern: 'https://github.com/TathanDev/SkyAesthetics/tree/1.21-rework/docs/:path'
    }
  }
})
