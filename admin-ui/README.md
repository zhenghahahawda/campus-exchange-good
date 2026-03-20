# Admin UI - 后台管理系统

基于 Nuxt.js 2 + Element UI 的现代化后台管理系统，支持多主题切换和响应式设计。

## ✨ 特性

- 🎨 多主题支持（蓝色、紫色主题）
- 📊 数据可视化（ECharts 图表）
- 🔐 用户认证系统
- 📦 商品管理模块
- 🎯 响应式设计
- 🌈 玻璃态 UI 组件

## 📚 开发规范文档

详细的 UI 设计规范、多主题开发指南及组件标准请参阅：
- 👉 [**设计规范**](./doc/DESIGN_SPEC.md)
- 👉 [**主题开发指南**](./doc/THEME_DEVELOPMENT.md)
- 👉 [**代码优化指南**](./doc/CODE_OPTIMIZATION.md)

## 🚀 快速开始

### 安装依赖

```bash
npm install
```

### 开发环境

```bash
# 启动开发服务器（自动打开浏览器）
npm run dev

# 访问 http://localhost:3000
```

### 生产构建

```bash
# 构建生产版本
npm run build

# 启动生产服务器
npm run start
```

### 静态生成

```bash
# 生成静态网站
npm run generate
```

## 📁 项目结构

```
admin-ui/
├── assets/          # 静态资源（CSS、图标等）
├── components/      # Vue 组件
│   ├── common/     # 通用组件
│   ├── dashboard/  # 仪表盘组件
│   ├── goods/      # 商品管理组件
│   ├── theme/      # 主题相关组件
│   └── ui/         # UI 基础组件
├── composables/     # 组合式函数
├── doc/            # 项目文档
├── layouts/        # 布局组件
├── mock/           # 模拟数据
├── pages/          # 页面路由
├── plugins/        # Nuxt 插件
├── static/         # 静态文件
├── store/          # Vuex 状态管理
└── utils/          # 工具函数
```

## 🤝 贡献

欢迎贡献代码！请查看 [贡献指南](./CONTRIBUTING.md) 了解详情。

## 📄 许可证

[MIT License](./LICENSE)

---

For detailed explanation on how things work, check out the [documentation](https://nuxtjs.org).

## Special Directories

You can create the following extra directories, some of which have special behaviors. Only `pages` is required; you can delete them if you don't want to use their functionality.

### `assets`

The assets directory contains your uncompiled assets such as Stylus or Sass files, images, or fonts.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/assets).

### `components`

The components directory contains your Vue.js components. Components make up the different parts of your page and can be reused and imported into your pages, layouts and even other components.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/components).

### `layouts`

Layouts are a great help when you want to change the look and feel of your Nuxt app, whether you want to include a sidebar or have distinct layouts for mobile and desktop.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/layouts).


### `pages`

This directory contains your application views and routes. Nuxt will read all the `*.vue` files inside this directory and setup Vue Router automatically.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/get-started/routing).

### `plugins`

The plugins directory contains JavaScript plugins that you want to run before instantiating the root Vue.js Application. This is the place to add Vue plugins and to inject functions or constants. Every time you need to use `Vue.use()`, you should create a file in `plugins/` and add its path to plugins in `nuxt.config.js`.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/plugins).

### `static`

This directory contains your static files. Each file inside this directory is mapped to `/`.

Example: `/static/robots.txt` is mapped as `/robots.txt`.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/static).

### `store`

This directory contains your Vuex store files. Creating a file in this directory automatically activates Vuex.

More information about the usage of this directory in [the documentation](https://nuxtjs.org/docs/2.x/directory-structure/store).
