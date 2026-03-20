<template>
  <div class="visitors-overview base-card">
    <div class="card-header">
      <div class="left">
        <h3 class="title">交易概览</h3>
        <span class="subtitle">8月25日 - 9月25日</span>
      </div>
      <div class="right">
        <!-- 可以在这里放其他操作 -->
      </div>
    </div>

    <div class="chart-content">
      <!-- 1. 左侧 Legend -->
      <div class="col-legend">
        <div class="item">
          <div class="header">
            <span class="dot purple"></span>
            <span class="label">数码产品</span>
          </div>
          <span class="value">80%</span>
        </div>
        <div class="item">
           <div class="header">
            <span class="dot orange"></span>
            <span class="label">生活用品</span>
          </div>
          <span class="value">20%</span>
        </div>
      </div>

      <!-- 2. 中间：头像环形图 (SVG) -->
      <div class="col-avatar-ring">
        <div class="ring-container">
          <!-- 内层橙色环 -->
          <svg class="ring-svg inner" viewBox="0 0 100 100">
             <!-- 路径：从右侧大概 3点钟方向 逆时针画到 12点钟 -->
             <path d="M 95 50 A 45 45 0 1 0 50 5" fill="none" stroke="#FF9F43" stroke-width="2" stroke-linecap="round" />
             <circle cx="95" cy="50" r="4" fill="#FF9F43" />
          </svg>
          
          <!-- 外层紫色环 -->
          <svg class="ring-svg outer" viewBox="0 0 120 120">
             <!-- 路径：从左下方大概 7点钟方向 顺时针画到 3点钟 -->
             <path d="M 30 90 A 55 55 0 1 1 115 60" fill="none" stroke="#6C5DD3" stroke-width="2" stroke-linecap="round" />
             <circle cx="115" cy="60" r="4" fill="#6C5DD3" />
          </svg>

          <!-- 中间头像 -->
          <div class="center-avatar">
            <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" alt="Avatar" />
          </div>
        </div>
      </div>

      <!-- 3. 中间：核心数据 + 柱状图 -->
      <div class="col-middle">
        <div class="main-stat">
          <span class="badge">+12%</span>
          <div class="big-number">92</div>
          <div class="label">今日交易总额</div>
        </div>
        
        <div class="bar-charts-row">
           <!-- 使用 ECharts 渲染两个小柱状图 -->
           <div class="mini-chart">
             <div ref="barChart1" class="chart-canvas" v-loading="loading"></div>
             <div class="chart-label">
               <span class="val">1,240</span>
               <span class="name">访问量</span>
             </div>
           </div>
           <div class="mini-chart">
             <div ref="barChart2" class="chart-canvas" v-loading="loading"></div>
             <div class="chart-label">
               <span class="val">86</span>
               <span class="name">成交量</span>
             </div>
           </div>
        </div>
      </div>
      
      <!-- 4. 右侧：其他数据 -->
      <div class="col-right">
        <div class="stat-item bg-blur-1">
          <div class="val">114</div>
          <div class="name">求购中</div>
        </div>
        <div class="stat-item bg-blur-2">
          <div class="val">98</div>
          <div class="name">待审核</div>
        </div>
         <div class="stat-item">
          <div class="badge-dot">
             <span class="dot"></span>
             <span class="text">+10%</span>
          </div>
          <div class="val">1,499</div>
          <div class="name">注册用户</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'VisitorsOverview',
  data() {
    return {
      loading: true
    }
  },
  mounted() {
    // 模拟数据加载延迟，展示 loading 效果
    setTimeout(() => {
      this.initBarChart1();
      this.initBarChart2();
      this.loading = false;
    }, 500);
    window.addEventListener('resize', this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts);
    if (this.barChart1) this.barChart1.dispose();
    if (this.barChart2) this.barChart2.dispose();
  },
  methods: {
    resizeCharts() {
      if (this._resizeTimeout) clearTimeout(this._resizeTimeout);
      this._resizeTimeout = setTimeout(() => {
        this.barChart1 && this.barChart1.resize();
        this.barChart2 && this.barChart2.resize();
      }, 200);
    },
    initBarChart1() {
      this.barChart1 = echarts.init(this.$refs.barChart1, null, { renderer: 'canvas' });
      const option = {
        animation: false,
        color: ['#0052CC'],
        grid: { top: 5, bottom: 20, left: 0, right: 0 },
        xAxis: { 
          type: 'category', 
          data: ['M', 'T', 'W', 'T', 'F', 'S', 'S'], 
          show: false 
        },
        yAxis: { type: 'value', show: false },
        series: [{
          data: [120, 200, 150, 80, 70, 110, 130],
          type: 'bar',
          barWidth: '6px',
          itemStyle: { borderRadius: 3 }
        }]
      };
      this.barChart1.setOption(option);
    },
    initBarChart2() {
      this.barChart2 = echarts.init(this.$refs.barChart2, null, { renderer: 'canvas' });
      const option = {
        animation: false,
        color: ['#FF9F43'],
        grid: { top: 5, bottom: 20, left: 0, right: 0 },
        xAxis: { type: 'category', show: false },
        yAxis: { type: 'value', show: false },
        series: [{
          data: [80, 120, 100, 150, 90, 60, 80],
          type: 'bar',
          barWidth: '6px',
          itemStyle: { borderRadius: 3 }
        }]
      };
      this.barChart2.setOption(option);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.visitors-overview {
  margin-top: 30px;
  min-height: 350px;
}

.card-header {
  margin-bottom: 30px;
  .title {
    font-size: 20px;
    color: $color-text-primary;
    margin-bottom: 4px;
  }
  .subtitle {
    font-size: 13px;
    color: $color-text-secondary;
  }
}

.chart-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

// 1. Left Legend
.col-legend {
  display: flex;
  flex-direction: column;
  gap: 30px;
  
  .item {
    .header {
      display: flex;
      align-items: center;
      margin-bottom: 8px;
      .dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        margin-right: 8px;
        &.purple { background-color: $color-primary; }
        &.orange { background-color: $color-secondary-warning; }
      }
      .label {
        font-size: 13px;
        color: $color-text-secondary;
      }
    }
    .value {
      display: block;
      font-size: 24px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin-left: 18px;
    }
  }
}

// 2. Avatar Ring (SVG)
.col-avatar-ring {
  width: 220px;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .ring-container {
    position: relative;
    width: 200px;
    height: 200px;
    
    .ring-svg {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      
      &.inner { width: 140px; height: 140px; }
      &.outer { width: 180px; height: 180px; }
    }
    
    .center-avatar {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 60px;
      height: 60px;
      border-radius: 50%;
      overflow: hidden;
      border: 4px solid #FFCD9E; // 浅橙色边框
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

// 3. Middle (Stat + Bars)
.col-middle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  
  .main-stat {
    text-align: center;
    position: relative;
    
    .big-number {
      font-size: 56px;
      font-weight: $font-weight-bold;
      color: #ffffff;
      line-height: 1;
    }
    .badge {
      position: absolute;
      top: 0;
      right: -30px;
      color: $color-secondary-success;
      font-weight: $font-weight-bold;
      font-size: 14px;
    }
    .label {
      margin-top: 8px;
      color: $color-text-secondary;
      font-size: 14px;
    }
  }
  
  .bar-charts-row {
    display: flex;
    gap: 30px;
    
    .mini-chart {
      .chart-canvas {
        width: 80px;
        height: 50px;
      }
      .chart-label {
        margin-top: 8px;
        .val {
          display: block;
          font-weight: $font-weight-bold;
          font-size: 18px;
          color: $color-text-primary;
        }
        .name {
          font-size: 12px;
          color: $color-text-secondary;
        }
      }
    }
  }
}

// 4. Right Stats
.col-right {
  display: flex;
  align-items: center;
  gap: 40px;
  
  .stat-item {
    text-align: center;
    position: relative;
    padding: 20px;
    
    // 模糊背景装饰
    &.bg-blur-1::before {
      content: '';
      position: absolute;
      width: 80px;
      height: 80px;
      background: rgba(108, 93, 211, 0.08); // 浅紫色
      filter: blur(20px);
      border-radius: 50%;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 0;
    }
    &.bg-blur-2::before {
      content: '';
      position: absolute;
      width: 80px;
      height: 80px;
      background: rgba(255, 159, 67, 0.08); // 浅橙色
      filter: blur(20px);
      border-radius: 50%;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      z-index: 0;
    }
    
    .val {
      position: relative;
      font-size: 20px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      z-index: 1;
      margin-bottom: 8px;
    }
    .name {
      position: relative;
      font-size: 12px;
      color: $color-text-secondary;
      z-index: 1;
    }
    
    .badge-dot {
      position: absolute;
      top: -10px;
      right: 0;
      display: flex;
      align-items: center;
      .dot {
        width: 12px;
        height: 12px;
        background: #9D92EE; // 亮紫色球
        border-radius: 50%;
        box-shadow: 0 4px 8px rgba(108, 93, 211, 0.3);
      }
      .text {
        margin-left: 4px;
        font-size: 12px;
        font-weight: bold;
        color: $color-text-secondary;
      }
    }
  }
}
</style>
