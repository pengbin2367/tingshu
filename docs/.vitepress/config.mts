import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "TingShu",
  description: "tingshu vite press docs",
  base: "/tingshu/",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Docs', link: '/01-GettingStart' }
    ],

    sidebar: [
      {
        text: 'Docs',
        items: [
          { text: '准备工作', link: '/01-GettingStart' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/pengbin2367/tingshu' }
    ]
  }
})
