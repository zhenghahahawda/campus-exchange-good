<template>
  <div class="card shadow">
    <div class="card-header bg-transparent border-0">
      <div class="row align-items-center">
        <div class="col">
          <h6 class="text-uppercase text-muted ls-1 mb-1">交易概览</h6>
          <h2 class="text-default mb-0">订单成交量</h2>
        </div>
        <div class="col-auto">
          <!-- Segmented Control -->
          <div class="nav-wrapper">
             <span 
               class="nav-link" 
               :class="{ active: activeRange === 'week' }"
               @click="activeRange = 'week'"
             >周</span>
             <span 
               class="nav-link" 
               :class="{ active: activeRange === 'month' }"
               @click="activeRange = 'month'"
             >月</span>
             <span 
               class="nav-link" 
               :class="{ active: activeRange === 'year' }"
               @click="activeRange = 'year'"
             >年</span>
          </div>
        </div>
      </div>
    </div>
    <div class="card-body">
      <div class="chart">
        <div ref="chart" style="height: 280px;"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart';
import { getThemeColors } from '@/utils/chartHelper';
import { isSuccess } from '@/composables/useApi';

export default {
  name: 'SalesChart',
  data() {
    return {
      activeRange: 'week',
      chartManager: null,
      allOrders: []
    }
  },
  watch: {
    activeRange() {
      this.updateChart()
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.chartManager = useChart();
      const colors = getThemeColors();
      this.chartManager.initChart(this.$refs.chart, this.buildOption(colors, [], []), () => this.updateChart());
      this.fetchOrders();
    });
  },
  beforeDestroy() {
    this.chartManager?.dispose();
  },
  methods: {
    async fetchOrders() {
      try {
        const res = await this.$axios.get('/api/admin/orders', { params: { pageNum: 1, pageSize: 9999 } })
        if (isSuccess(res)) {
          const pageData = res.data || {}
          this.allOrders = pageData.records || []
        }
        this.updateChart()
      } catch (e) {
        console.error('获取订单数据失败:', e)
      }
    },
    updateChart() {
      const colors = getThemeColors();
      const { labels, seriesData } = this.aggregateData()
      this.chartManager?.setOption(this.buildOption(colors, labels, seriesData))
    },
    getBuckets() {
      const now = new Date()
      const labels = []
      const buckets = []
      if (this.activeRange === 'week') {
        for (let i = 6; i >= 0; i--) {
          const d = new Date(now.getTime() - i * 24 * 3600 * 1000)
          labels.push(`${d.getMonth() + 1}/${d.getDate()}`)
          buckets.push({ start: new Date(d.getFullYear(), d.getMonth(), d.getDate()), end: new Date(d.getFullYear(), d.getMonth(), d.getDate() + 1) })
        }
      } else if (this.activeRange === 'month') {
        for (let i = 29; i >= 0; i--) {
          const d = new Date(now.getTime() - i * 24 * 3600 * 1000)
          labels.push(`${d.getMonth() + 1}/${d.getDate()}`)
          buckets.push({ start: new Date(d.getFullYear(), d.getMonth(), d.getDate()), end: new Date(d.getFullYear(), d.getMonth(), d.getDate() + 1) })
        }
      } else {
        for (let i = 11; i >= 0; i--) {
          const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
          labels.push(`${d.getMonth() + 1}月`)
          buckets.push({ start: d, end: new Date(d.getFullYear(), d.getMonth() + 1, 1) })
        }
      }
      return { labels, buckets }
    },
    countByStatus(buckets, status) {
      return buckets.map(b => this.allOrders.filter(o => {
        const t = new Date(o.createTime || o.createdAt)
        return t >= b.start && t < b.end && o.status === status
      }).length)
    },
    aggregateData() {
      const { labels, buckets } = this.getBuckets()
      const statuses = [
        { key: 'total', name: '总订单' },
        { key: 'pending', name: '待交易' },
        { key: 'to_process', name: '待处理' },
        { key: 'processing', name: '处理中' },
        { key: 'completed', name: '已完成' },
        { key: 'cancelled', name: '已取消' }
      ]
      const seriesData = statuses.map(s => {
        if (s.key === 'total') {
          // 总订单数 = 所有状态的订单
          return {
            ...s,
            data: buckets.map(b => this.allOrders.filter(o => {
              const t = new Date(o.createTime || o.createdAt)
              return t >= b.start && t < b.end
            }).length)
          }
        }
        return {
          ...s,
          data: this.countByStatus(buckets, s.key)
        }
      })
      return { labels, seriesData }
    },
    buildOption(colors, labels, seriesData) {
      const statusColors = {
        total: '#5e72e4',
        pending: '#ffc107',
        to_process: '#17a2b8',
        processing: '#6f42c1',
        completed: '#2dce89',
        cancelled: '#e74c3c'
      }
      return {
        animation: true,
        animationDuration: 1000,
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          textStyle: { color: '#333' }
        },
        legend: {
          data: seriesData.map(s => s.name),
          textStyle: { color: colors.textSecondary, fontSize: 11 },
          bottom: 0
        },
        grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true, borderColor: 'transparent' },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: labels,
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: colors.textSecondary }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { type: 'dashed', color: colors.border } },
          axisLabel: { color: colors.textSecondary }
        },
        series: seriesData.map(s => ({
          name: s.name,
          data: s.data,
          type: 'line',
          smooth: true,
          lineStyle: { color: statusColors[s.key], width: 2 },
          itemStyle: { color: statusColors[s.key] },
          showSymbol: false,
          areaStyle: { color: statusColors[s.key], opacity: 0.05 }
        }))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  word-wrap: break-word;
  background-clip: border-box;
  @include glass-card;
}

.bg-default {
  background-color: #172b4d !important;
}

.shadow {
  box-shadow: 0 0 2rem 0 rgba(136, 152, 170, .15);
}

.card-header {
  padding: 1rem 1.5rem;
  margin-bottom: 0;
  background-color: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, .05);
  
  &.bg-transparent {
    background-color: transparent;
  }
  
  &.border-0 {
    border: 0;
  }
}

.card-body {
  flex: 1 1 auto;
  min-height: 1px;
  padding: 1rem 1.5rem;
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

.text-uppercase {
  text-transform: uppercase;
}

.text-default {
  color: $color-text-primary;
}

.text-muted {
  color: $color-text-secondary;
}

.mb-0 {
  margin-bottom: 0;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.ls-1 {
  letter-spacing: .0625rem;
}

.nav-wrapper {
    display: inline-flex;
    background: $color-pill-bg; // 使用主题变量 (深色模式: 半透明白 / 浅色模式: 浅灰)
    backdrop-filter: blur(10px);
    border-radius: 9999px;
    padding: 4px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    
    .nav-link {
      padding: 6px 24px;
      color: $color-text-secondary;
      cursor: pointer;
      font-size: 0.875rem;
      font-weight: 600;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border-radius: 9999px;
      
      &:hover {
        color: $color-text-primary;
        background: rgba(255, 255, 255, 0.05);
      }
      
      &.active {
        background: $color-pill-active-bg; // 使用主题变量 (深色模式: 白 / 浅色模式: 淡粉/淡紫)
        color: $color-pill-active-text; // 使用主题变量 (深色模式: 蓝 / 浅色模式: 深紫)
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
      }
    }
  }
</style>