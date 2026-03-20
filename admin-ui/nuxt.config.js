export default {
  // Server configuration
  server: {
    port: process.env.PORT || 3000,
    host: process.env.HOST || '0.0.0.0'
  },

  // Global page headers: https://go.nuxtjs.dev/config-head
  head: {
    title: '主页',
    htmlAttrs: {
      lang: 'en'
    },
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: '' },
      { name: 'format-detection', content: 'telephone=no' }
    ],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
    ]
  },

  // Global CSS: https://go.nuxtjs.dev/config-css
  css: [
    '@/static/css/reset.css',
    'element-ui/lib/theme-chalk/index.css',
    '@/assets/css/main.scss',
    '@/assets/css/transitions.css'
  ],

  // Global middleware
  router: {
    middleware: ['auth-uid']
  },

  // Page transition
  pageTransition: {
    name: 'page',
    mode: 'out-in'
  },

  // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
  plugins: [
    '@/plugins/element-ui',
    { src: '@/plugins/axios', mode: 'client' },
    { src: '@/plugins/theme', mode: 'client' }
  ],

  // Auto import components: https://go.nuxtjs.dev/config-components
  components: true,

  // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
  buildModules: [
  ],

  // Modules: https://go.nuxtjs.dev/config-modules
  modules: [
    // https://go.nuxtjs.dev/axios
    '@nuxtjs/axios',
    '@nuxtjs/proxy'
  ],

  // Axios module configuration: https://go.nuxtjs.dev/config-axios
  axios: {
    // Workaround to avoid enforcing hard-coded localhost:3000: https://github.com/nuxt-community/axios-module/issues/308
    baseURL: process.env.API_URL || 'http://localhost:10010/',
    proxy: true
  },

  // Proxy configuration to avoid CORS issues
  proxy: {
    '/api/': {
      target: process.env.API_URL || 'http://localhost:10010/',
      pathRewrite: { '^/api/': '/api/' },
      changeOrigin: true
    },
    '/music-api/': {
      target: 'http://106.52.174.132:3015',
      pathRewrite: { '^/music-api/': '/' },
      changeOrigin: true
    },
    '/kiro-api/': {
      target: 'http://106.52.174.132:3016',
      pathRewrite: { '^/kiro-api/': '/' },
      changeOrigin: true
    }
  },

  // Build Configuration: https://go.nuxtjs.dev/config-build
  build: {
    transpile: [/^element-ui/],
    optimization: {
      splitChunks: {
        chunks: 'all',
        automaticNameDelimiter: '.',
        name: true,
        maxSize: 244000,
        cacheGroups: {
          vendor: {
            name: 'node_vendors',
            test: /[\\/]node_modules[\\/]/,
            chunks: 'all',
            priority: 10
          }
        }
      }
    }
  }
}
