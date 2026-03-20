<template>
  <div class="stat-card" @click="handleClick">
    <div class="card-body">
      <div class="row align-items-center">
        <div class="col">
          <h5 class="card-title text-uppercase text-muted mb-0">{{ title }}</h5>
          <span class="h2 font-weight-bold mb-0">{{ value }}</span>
        </div>
        <div class="col-auto">
          <div class="icon icon-shape text-white rounded-circle" :class="iconBg">
            <i :class="icon"></i>
          </div>
        </div>
      </div>
      <div class="mt-3 mb-0 text-sm">
        <span :class="percentageColor" class="mr-2">
          <i :class="percentageIcon"></i> {{ percentage }}
        </span>
        <span class="text-nowrap">{{ footerLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatCard',
  props: {
    title: {
      type: String,
      required: true
    },
    value: {
      type: [String, Number],
      required: true
    },
    icon: {
      type: String,
      default: 'el-icon-s-data'
    },
    iconBg: {
      type: String,
      default: 'bg-primary'
    },
    percentage: {
      type: String,
      default: ''
    },
    percentageColor: {
      type: String,
      default: 'text-success'
    },
    footerLabel: {
      type: String,
      default: ''
    }
  },
  computed: {
    percentageIcon() {
      return this.percentageColor === 'text-success' ? 'el-icon-top' : 'el-icon-bottom';
    }
  },
  methods: {
    handleClick() {
      this.$emit('click');
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.stat-card {
  @include glass-card; // 使用主题定义的玻璃拟态样式
  margin-bottom: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer; // 添加鼠标指针样式

  // 添加蓝色边框，类似参考图片 - 使用!important覆盖glass-card的边框
  border: 2px solid rgba(74, 144, 226, 0.5) !important;
  border-radius: 16px !important;
  position: relative;
  overflow: hidden;
  
  // 添加内部光晕效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 14px;
    border: 1px solid rgba(74, 144, 226, 0.3);
    pointer-events: none;
  }

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 40px 0 rgba(0, 0, 0, 0.3); // 悬浮时加深阴影
    border-color: rgba(74, 144, 226, 0.8) !important; // 悬浮时加强边框
    
    &::before {
      border-color: rgba(74, 144, 226, 0.5);
    }
  }

  .card-body {
    padding: 1rem 1.5rem;
    flex: 1 1 auto;
  }

  .row {
    display: flex;
    flex-wrap: wrap;
    margin-right: -15px;
    margin-left: -15px;
    
    &.align-items-center {
      align-items: center;
    }
  }

  .col {
    flex-basis: 0;
    flex-grow: 1;
    max-width: 100%;
    padding-right: 15px;
    padding-left: 15px;
  }

  .col-auto {
    flex: 0 0 auto;
    width: auto;
    max-width: 100%;
    padding-right: 15px;
    padding-left: 15px;
  }

  .card-title {
    margin-bottom: 0.5rem;
    font-size: .8125rem;
    font-weight: 600;
    color: $color-text-secondary; // 使用主题次要文字色
  }

  .text-uppercase {
    text-transform: uppercase;
  }

  .text-muted {
    color: $color-text-secondary; // 使用主题次要文字色
  }

  .mb-0 {
    margin-bottom: 0;
  }

  .h2 {
    font-size: 1.5rem; // 稍微调大一点
    font-family: inherit;
    font-weight: 700;
    line-height: 1.5;
    color: $color-text-primary; // 使用主题主要文字色
  }

  .font-weight-bold {
    font-weight: 600;
  }

  .icon {
    width: 3.5rem;
    height: 3.5rem;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50% !important;
    font-size: 1.5rem;
    transition: transform 0.3s ease;
  }
  
  &:hover .icon {
    transform: scale(1.1) rotate(5deg);
  }

  .icon-shape {
    padding: 12px;
    text-align: center;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
  }

  .text-white {
    color: #fff;
  }

  .shadow {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  }

  // 使用主题变量定义图标背景色和对应的光晕效果
  .bg-primary {
    background-color: rgba($color-primary-rgb, 0.15);
    color: $color-primary;
    box-shadow: 0 4px 20px rgba($color-primary-rgb, 0.15);
  }

  .bg-danger {
    background-color: rgba($color-secondary-danger-rgb, 0.15);
    color: $color-secondary-danger;
    box-shadow: 0 4px 20px rgba($color-secondary-danger-rgb, 0.15);
  }

  .bg-warning {
    background-color: rgba($color-secondary-warning-rgb, 0.15);
    color: $color-secondary-warning;
    box-shadow: 0 4px 20px rgba($color-secondary-warning-rgb, 0.15);
  }
  
  .bg-yellow {
    background-color: rgba(255, 214, 0, 0.15);
    color: #ffd600;
    box-shadow: 0 4px 20px rgba(255, 214, 0, 0.15);
  }
  
  .bg-info {
    background-color: rgba($color-secondary-info-rgb, 0.15);
    color: $color-secondary-info;
    box-shadow: 0 4px 20px rgba($color-secondary-info-rgb, 0.15);
  }

  .bg-success {
    background-color: rgba($color-secondary-success-rgb, 0.15);
    color: $color-secondary-success;
    box-shadow: 0 4px 20px rgba($color-secondary-success-rgb, 0.15);
  }

  .mt-3 {
    margin-top: 1rem;
  }

  .text-sm {
    font-size: .875rem;
  }

  .text-success {
    color: $color-secondary-success;
  }

  .text-warning {
    color: $color-secondary-warning;
  }
  
  .text-danger {
    color: $color-secondary-danger;
  }

  .mr-2 {
    margin-right: 0.5rem;
  }

  .text-nowrap {
    white-space: nowrap;
    color: $color-text-secondary;
  }
}
</style>