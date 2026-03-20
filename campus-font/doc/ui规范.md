# 前端设计规范文档 (Frontend Design Specification)

本文档规范了项目的 UI 风格、多主题架构、色彩系统及组件开发标准。项目采用 **CSS Custom Properties (CSS 变量)** 结合 **SCSS Mixins** 实现高度可配置的 **多主题毛玻璃拟态 (Multi-Theme Glassmorphism)** 风格。

## 1. 设计概述 (Design Overview)

- **核心风格**: 现代毛玻璃拟态 (Modern Glassmorphism)。
- **主题架构**: 支持实时切换的 CSS 变量主题系统。
- **视觉特点**:
  - **动态背景**: 随主题色变化的流体渐变背景。
  - **材质**: 高度可配置的磨砂玻璃质感 (`backdrop-filter`)。
  - **光影**: 基于 CSS 变量的动态高光与阴影。
  - **薄雾感**: 进度条等元素使用低透明度与背景模糊，营造朦胧质感。
- **交互**: 
  - **View Transitions**: 主题切换时使用原生的视图过渡 API 实现波纹扩散效果。
  - **微交互**: 悬停、聚焦时的平滑过渡与缩放。
  - **图标动态**: 侧边栏 Logo 悬停时的旋转与缩放动画。

---

## 2. 主题与色彩系统 (Theme & Color System)

项目不再依赖单一的静态颜色值，而是通过语义化的 **CSS 变量** 进行管理。所有颜色定义位于 `assets/css/themes/variables.css`。

### 2.1 核心语义变量 (Core Semantic Variables)

开发时**必须**使用以下变量，严禁硬编码 Hex/RGB 值：

| 变量名 | 描述 | 示例用途 |
| :--- | :--- | :--- |
| `--color-primary` | **品牌主色** | 按钮、选中态、关键图标、图表主色 |
| `--color-primary-rgb` | 主色 RGB 值 | 用于 `rgba()` 透明度计算、阴影、光晕 |
| `--color-bg-page` | **页面背景** | `body` 背景、全屏容器背景 |
| `--color-bg-surface` | **表面背景** | 非玻璃态的卡片、面板背景 |
| `--color-text-primary` | **主要文字** | 标题、正文 |
| `--color-text-secondary` | **次要文字** | 标签、说明文案、图表坐标轴 |
| `--color-text-disabled` | **禁用文字** | 占位符、禁用状态 |
| `--color-border` | **边框色** | 分割线、边框、图表分割线 |

### 2.2 辅助色 (Secondary Colors)

所有辅助色均配备对应的 RGB 变量（如 `--color-secondary-success-rgb`），以便制作半透明背景或光晕。

| 变量名 | 描述 | RGB 变量 |
| :--- | :--- | :--- |
| `--color-secondary-success` | 成功 (Green) | `--color-secondary-success-rgb` |
| `--color-secondary-warning` | 警告 (Orange) | `--color-secondary-warning-rgb` |
| `--color-secondary-danger` | 危险/错误 (Red) | `--color-secondary-danger-rgb` |
| `--color-secondary-info` | 信息 (Blue/Cyan) | `--color-secondary-info-rgb` |

### 2.3 组件语义变量 (Component Semantic Variables)

针对特定组件的专用变量，确保在深色/浅色主题下都能正确显示：

- **导航栏**: `--color-nav-active-bg`, `--color-nav-active-text`
- **侧边栏**: `--color-sidebar-user-card-bg`
- **胶囊控件**: `--color-pill-bg`, `--color-pill-active-bg`
- **通知徽标**: 统一使用 `--color-primary`，但在不同主题下自动适配。

---

## 3. 视觉组件规范 (Visual Component Standards)

### 3.1 图标与徽标 (Icons & Badges)
- **通知徽标**:
  - 必须使用 `--color-primary` 作为背景色。
  - 必须包含 `2px` 的切割边框（颜色同背景色），以实现“嵌入”效果。
  - 必须包含同色系的半透明光晕 (`box-shadow`)。
  - 位置应向右上角溢出，不遮挡图标主体。
- **功能图标**:
  - 背景色应为主题色的低透明度版本（如 `rgba(var(--color-primary-rgb), 0.15)`）。
  - 图标颜色应为对应的主题色实色。
  - 支持 `svg` 内联代码以实现 CSS 变量对路径颜色的控制。

### 3.2 数据图表 (Data Charts - ECharts)
- **主题适配**:
  - 必须监听 `data-theme` 属性变化。
  - 颜色配置必须读取 `getComputedStyle` 中的 CSS 变量。
  - 坐标轴文字使用 `--color-text-secondary`。
  - 分割线使用 `--color-border`。
  - 系列颜色使用 `--color-primary` 或 `--color-secondary-*`。
- **样式**:
  - 折线图应包含从主题色到透明的线性渐变区域 (`areaStyle`)。
  - 柱状图应使用圆角设计。

### 3.3 进度条 (Progress Bars)
- **样式**:
  - 必须使用圆角胶囊样式 (`border-radius: 999px`)。
  - **禁止使用线性渐变色**，应使用纯色主题色配合透明度。
  - **薄雾感**: 设置 `opacity: 0.8` 并添加 `backdrop-filter: blur(4px)`。
  - **高光**: 添加内阴影 `inset 0 1px 0 rgba(255, 255, 255, 0.3)` 模拟玻璃边缘反光。

### 3.4 登录与认证系统 (Login & Auth)
- **登录卡片**:
  - 采用 **Glassmorphism** 风格，居中悬浮。
  - 使用 `backdrop-filter: blur(20px)` 增强背景模糊。
  - 边框和阴影应使用 CSS 变量以适配深/浅色模式。
- **人机验证 (Captcha)**:
  - 采用 **滑块验证 (Slider Captcha)**。
  - **未验证状态**: 提示“请向右滑动验证”，滑块可拖动。
  - **验证成功状态**: 背景变绿 (`--color-secondary-success`)，显示“验证通过”，滑块锁定并显示勾选图标。
  - **交互**: 验证未通过时禁用登录按钮。
- **记住密码**:
  - 采用自定义 **单选框样式** (Radio Circle) 替代原生 Checkbox。
  - 选中时圆圈内显示实心圆点，标签文字高亮为主题色。
  - 功能：使用 `localStorage` 保存加密后的凭证（注：实际项目中应使用更安全的 Token 机制，当前仅为演示）。

---

## 4. 开发规范 (Development Guidelines)

### 4.1 新建组件规范
1.  **文件位置**: `components/` 目录下按功能分类。
2.  **样式引入**: 必须在 `<style>` 顶部引入 `@import '@/assets/css/themes/index.scss';`。
3.  **颜色使用**: 
    - ❌ `color: #0075ff;` (禁止硬编码)
    - ✅ `color: $color-primary;` (SCSS 映射变量) 或 `color: var(--color-primary);` (原生 CSS 变量)
4.  **透明度处理**:
    - ❌ `lighten($color, 10%)` (Sass 函数无法处理 CSS 变量)
    - ✅ `rgba(var(--color-primary-rgb), 0.1);` (推荐)
    - ✅ `color-mix(in srgb, var(--color-primary), transparent 20%)` (现代浏览器)

### 4.2 SVG 图标规范
- 尽量使用内联 `<svg>` 标签而非 `<img>` 引用，以便 CSS 控制颜色。
- SVG 路径的 `fill` 属性应设置为 `currentColor` 或直接绑定 CSS 变量。
- 示例：
  ```html
  <svg ...>
    <path d="..." style="fill: var(--color-primary)" />
  </svg>
  ```

### 4.3 性能优化
- **背景动画**: `DynamicBackground.vue` 使用了 `will-change: transform` 优化性能。
- **ECharts**: 初始化时指定 `{ renderer: 'canvas' }` 以获得更好的滚动性能。
- **Resize**: 图表 resize 事件必须添加防抖 (debounce) 处理。

---

## 5. 目录结构说明 (Directory Structure)

```
assets/css/
├── themes/
│   ├── variables.css      # 核心：定义所有 CSS 变量和主题
│   ├── index.scss         # 桥接：将 CSS 变量映射为 SCSS 变量/Mixin
│   ├── blue.scss          # (旧) 兼容性文件
│   └── purple.scss        # (旧) 兼容性文件
└── main.scss              # 全局样式入口
```

*文档更新时间: 2026-03-05*


---

## 6. 交互组件规范 (Interactive Components)

### 6.1 灵动岛通知系统 (Dynamic Island Notification)

**设计理念**:
- 灵感来自 iOS 灵动岛，提供优雅的非侵入式通知体验。
- 位于页面顶部中央，始终保持最高层级 (`z-index: 99999`)。
- 支持自动展开/收起动画，悬停时保持展开状态。

**使用规范**:
- **禁止使用** Element UI 的 `$message` 组件，统一使用灵动岛。
- 调用方式：`this.$root.$emit('show-island-message', '消息内容', '类型')`
- 支持类型：`'info'`, `'success'`, `'warning'`, `'danger'`

**视觉规范**:
- **默认状态**: 显示当前时间，宽度 90px，高度 32px。
- **展开状态**: 宽度 300px，高度 60px，显示消息内容和图标。
- **浮动状态**: 当页面滚动时，固定在顶部，z-index 提升至 99999。
- **动画**: 使用 `cubic-bezier(0.175, 0.885, 0.32, 1.275)` 缓动函数实现弹性动画。

**交互规范**:
- 消息自动显示 3 秒后收起。
- 鼠标悬停时保持展开，移开后 3 秒收起。
- 支持同时显示音乐播放器等扩展内容。

**代码示例**:
```javascript
// 成功提示
this.$root.$emit('show-island-message', '操作成功', 'success');

// 警告提示
this.$root.$emit('show-island-message', '请输入必填项', 'warning');

// 错误提示
this.$root.$emit('show-island-message', '操作失败', 'danger');

// 信息提示
this.$root.$emit('show-island-message', '正在处理...', 'info');
```

### 6.2 表单与弹窗规范 (Forms & Dialogs)

**表单布局**:
- 使用 `el-form` 配合 `el-row` 和 `el-col` 实现表格式布局。
- 标签宽度统一为 `100px`，左对齐 (`label-position="left"`)。
- 相关字段分组显示（如：出售价格和原价、新旧程度和分类）。
- 使用 `:gutter="20"` 设置列间距。

**输入框样式**:
```scss
.el-input__inner {
  height: 48px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba($color-border, 0.3);
  color: $color-text-primary;
  transition: all 0.3s ease;
  
  &:focus {
    border-color: $color-primary;
    background: rgba(255, 255, 255, 0.08);
    box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1);
  }
}
```

**弹窗规范**:
- 使用 `custom-class="glass-dialog"` 应用毛玻璃效果。
- 宽度建议：小型 400px，中型 750px，大型 1000px。
- 顶部间距：`top="5vh"`
- 禁用遮罩点击关闭：`:close-on-click-modal="false"`
- 禁用默认遮罩：`:modal="false"`（使用自定义毛玻璃背景）

**按钮规范**:
```scss
.glass-btn {
  padding: 12px 32px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.btn-cancel {
    background: rgba($color-text-secondary, 0.1);
    border: 1px solid rgba($color-border, 0.3);
    color: $color-text-secondary;
  }
  
  &.btn-save {
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.95), 
      rgba(var(--color-primary-rgb), 0.85));
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
  }
}
```

### 6.3 图片上传组件 (Image Upload)

**布局规范**:
- 使用 Flex 布局，`gap: 14px`，自动换行。
- 图片框尺寸：`110px × 110px`
- 圆角：`14px`
- 最多支持 5 张图片。

**图片项样式**:
```scss
.image-item-box {
  width: 110px;
  height: 110px;
  border-radius: 14px;
  border: 2px solid rgba($color-border, 0.3);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    border-color: $color-primary;
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.2);
  }
  
  .image-index {
    position: absolute;
    top: 8px;
    left: 8px;
    width: 26px;
    height: 26px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(8px);
    color: #fff;
    font-weight: 700;
  }
}
```

**上传框样式**:
```scss
.upload-box {
  width: 110px;
  height: 110px;
  border-radius: 14px;
  border: 2px dashed rgba($color-border, 0.5);
  background: rgba($color-primary, 0.03);
  cursor: pointer;
  
  &:hover {
    border-color: $color-primary;
    background: rgba($color-primary, 0.08);
    transform: translateY(-4px);
  }
}
```

### 6.4 数据验证与提示 (Validation & Feedback)

**输入验证规范**:
- 数字范围验证：使用 `@input` 和 `@blur` 事件实时验证。
- 超出范围自动修正，并通过灵动岛提示用户。
- 必须提供清晰的错误提示信息。

**验证示例**:
```javascript
validateCondition() {
  let val = this.currentForm.conditionVal;
  
  if (val === '' || val === null || isNaN(val)) {
    this.currentForm.conditionVal = 95; // 默认值
    return;
  }
  
  val = Number(val);
  
  if (val < 1) {
    this.currentForm.conditionVal = 1;
    this.$root.$emit('show-island-message', '新旧程度最小为1', 'warning');
  } else if (val > 100) {
    this.currentForm.conditionVal = 100;
    this.$root.$emit('show-island-message', '新旧程度最大为100', 'warning');
  } else {
    this.currentForm.conditionVal = Math.round(val);
  }
}
```

**字数统计**:
- 使用 `show-word-limit` 显示字数限制。
- 统计背景必须透明：`background: transparent !important`
- 颜色：`$color-text-disabled`
- 字体大小：`12px`

**必填项标识**:
- 使用 Element UI 的 `required` 规则。
- 错误提示通过灵动岛显示，不使用默认的红色文字。
- 表单提交前进行完整性验证。

---

## 7. 响应式设计规范 (Responsive Design)

### 7.1 断点定义 (Breakpoints)

| 断点名称 | 屏幕宽度 | 设备类型 | 布局调整 |
| :--- | :--- | :--- | :--- |
| `xs` | < 768px | 手机 | 单列布局，隐藏侧边栏 |
| `sm` | 768px - 992px | 平板竖屏 | 两列布局，可折叠侧边栏 |
| `md` | 992px - 1200px | 平板横屏 | 标准布局 |
| `lg` | 1200px - 1920px | 桌面 | 标准布局 |
| `xl` | > 1920px | 大屏 | 宽松布局 |

### 7.2 响应式规则

**布局调整**:
- 使用 `el-row` 和 `el-col` 的响应式属性：`:xs`, `:sm`, `:md`, `:lg`, `:xl`
- 小屏幕下表单改为单列布局
- 图片上传组件自动换行

**字体缩放**:
- 标题字体：桌面 34px，平板 28px，手机 24px
- 正文字体：桌面 14px，平板 13px，手机 12px
- 使用媒体查询实现平滑过渡

**交互优化**:
- 移动端增大点击区域（最小 44px × 44px）
- 触摸设备禁用悬停效果
- 使用 `@media (hover: none)` 检测触摸设备

---

## 8. 性能优化指南 (Performance Optimization)

### 8.1 CSS 优化
- 使用 `will-change` 提示浏览器优化动画属性
- 避免过度使用 `backdrop-filter`（性能消耗较大）
- 使用 `transform` 和 `opacity` 实现动画（GPU 加速）
- 合理使用 `contain` 属性隔离渲染

### 8.2 JavaScript 优化
- 图表 resize 事件必须添加防抖 (debounce)
- 使用 `requestAnimationFrame` 优化动画
- 避免在滚动事件中进行复杂计算
- 使用 `IntersectionObserver` 实现懒加载

### 8.3 资源优化
- 图片使用 WebP 格式，提供 JPEG/PNG 降级
- SVG 图标内联使用，减少 HTTP 请求
- 使用 CDN 加载第三方库
- 启用 Gzip/Brotli 压缩

---

## 9. 可访问性规范 (Accessibility)

### 9.1 语义化 HTML
- 使用正确的 HTML5 语义标签
- 表单必须包含 `<label>` 标签
- 按钮使用 `<button>` 而非 `<div>`
- 图片必须包含 `alt` 属性

### 9.2 键盘导航
- 所有交互元素支持键盘访问
- 使用 `Tab` 键顺序合理
- 焦点状态清晰可见
- 支持 `Enter` 和 `Space` 键触发操作

### 9.3 颜色对比度
- 文字与背景对比度至少 4.5:1
- 大号文字（18px+）对比度至少 3:1
- 不仅依赖颜色传达信息
- 提供高对比度主题选项

---

## 10. 代码示例 (Code Examples)

### 10.1 完整的表单弹窗示例

```vue
<template>
  <el-dialog
    title="编辑商品"
    :visible="visible"
    @update:visible="$emit('update:visible', $event)"
    width="750px"
    custom-class="glass-dialog"
    top="5vh"
    :modal="false"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="100px" label-position="left">
      <!-- 商品名称 -->
      <el-form-item label="商品名称">
        <el-input 
          v-model="form.name" 
          placeholder="请输入商品名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 价格信息 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="出售价格">
            <el-input 
              v-model.number="form.price" 
              type="number"
              placeholder="5200"
            >
              <template slot="prefix">¥</template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="原价">
            <el-input 
              v-model.number="form.originalPrice" 
              type="number"
              placeholder="8999"
            >
              <template slot="prefix">¥</template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    
    <span slot="footer">
      <button class="glass-btn btn-cancel" @click="$emit('update:visible', false)">
        <i class="el-icon-close"></i>
        <span>取消</span>
      </button>
      <button class="glass-btn btn-save" @click="handleSave">
        <i class="el-icon-check"></i>
        <span>保存</span>
      </button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  props: {
    visible: Boolean,
    form: Object
  },
  methods: {
    handleSave() {
      // 验证表单
      if (!this.form.name) {
        this.$root.$emit('show-island-message', '请输入商品名称', 'warning');
        return;
      }
      
      // 保存数据
      this.$emit('save', this.form);
      this.$root.$emit('show-island-message', '保存成功', 'success');
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

// 样式代码...
</style>
```

### 10.2 灵动岛集成示例

```vue
<template>
  <div class="page">
    <button @click="showSuccess">成功提示</button>
    <button @click="showWarning">警告提示</button>
    <button @click="showError">错误提示</button>
  </div>
</template>

<script>
export default {
  methods: {
    showSuccess() {
      this.$root.$emit('show-island-message', '操作成功', 'success');
    },
    showWarning() {
      this.$root.$emit('show-island-message', '请注意检查', 'warning');
    },
    showError() {
      this.$root.$emit('show-island-message', '操作失败', 'danger');
    }
  }
}
</script>
```

---

## 11. 常见问题 (FAQ)

**Q: 为什么不能使用 `$message` 组件？**  
A: 项目统一使用灵动岛通知系统，提供更优雅的用户体验。灵动岛位于页面顶部，不会遮挡内容，且视觉效果更符合整体设计风格。

**Q: 如何确保主题切换时组件样式正确？**  
A: 必须使用 CSS 变量而非硬编码颜色值。所有颜色都应该引用 `--color-*` 变量，这样主题切换时会自动更新。

**Q: 弹窗的 z-index 应该设置多少？**  
A: Element UI 弹窗默认 z-index 为 2000-3000，灵动岛使用 99999 确保始终在最上层。自定义弹窗建议使用 3000-5000 之间的值。

**Q: 如何处理表单验证？**  
A: 使用 `@input` 和 `@blur` 事件实时验证，错误提示通过灵动岛显示。避免使用 Element UI 的默认红色错误文字。

**Q: 移动端如何适配？**  
A: 使用 `el-col` 的响应式属性（`:xs`, `:sm` 等），小屏幕下改为单列布局。增大点击区域，禁用悬停效果。

---

*文档最后更新时间: 2026-03-05*
*版本: v2.0*
