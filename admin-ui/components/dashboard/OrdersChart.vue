<template>
  <div class="chart-card">
    <div class="chart-header">
      <span class="chart-subtitle">概览</span>
      <h2 class="chart-title">用户统计</h2>
    </div>
    <div class="chart-body">
      <div ref="chartContainer" class="chart-container"></div>
    </div>
  </div>
</template>

<script>
import { useChart } from '@/composables/useChart';
import { getThemeColors } from '@/utils/chartHelper';
import { isSuccess } from '@/composables/useApi';

export default {
  name: 'OrdersChart',
  data() {
    return {
      chartManager: null,
      allUsers: []
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.chartManager = useChart();
      const colors = getThemeColors();
      this.chartManager.initChart(this.$refs.chartContainer, this.buildOption(colors, [], {}, {}), () => this.updateChart());
      this.fetchUsers();
    });
  },
  beforeDestroy() {
    this.chartManager?.dispose();
  },
  methods: {
    async fetchUsers() {
      try {
        const res = await this.$axios.get('/api/user/list')
        if (isSuccess(res)) {
          this.allUsers = (res.data || []).filter(u => u.userType !== 1)
        }
        this.updateChart()
      } catch (e) {
        console.error('获取用户统计数据失败:', e)
      }
    },
    updateChart() {
      const colors = getThemeColors();
      const { labels, userGroup, activeGroup } = this.aggregateData()
      this.chartManager?.setOption(this.buildOption(colors, labels, userGroup, activeGroup))
    },
    aggregateData() {
      const now = new Date()
      const labels = []
      const buckets = []

      for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        labels.push(`${d.getMonth() + 1}月`)
        const end = new Date(d.getFullYear(), d.getMonth() + 1, 1)
        buckets.push({ start: d, end })
      }

      // 用户量：累计 & 新增
      const totalData = buckets.map(b => this.allUsers.filter(u => new Date(u.createdAt || u.createTime) < b.end).length)
      const newData = buckets.map(b => this.allUsers.filter(u => {
        const t = new Date(u.createdAt || u.createTime)
        return t >= b.start && t < b.end
      }).length)
      const oldData = totalData.map((t, i) => t - newData[i])

      // 活跃用户(status=2)：截至该月末的累计活跃用户 & 该月内新变为活跃的
      const activeData = buckets.map(b => this.allUsers.filter(u => u.status === 2 && new Date(u.createdAt || u.createTime) < b.end).length)
      const newActiveData = buckets.map(b => this.allUsers.filter(u => {
        if (u.status !== 2) return false
        const reg = new Date(u.createdAt || u.createTime)
        return reg >= b.start && reg < b.end
      }).length)
      const oldActiveData = activeData.map((t, i) => t - newActiveData[i])

      return {
        labels,
        userGroup: { oldData, newData },
        activeGroup: { oldData: oldActiveData, newData: newActiveData }
      }
    },
    buildOption(colors, labels, userGroup, activeGroup) {
      return {
        animation: true,
        animationDuration: 1000,
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          textStyle: { color: '#333' }
        },
        legend: {
          data: ['原有用户', '新增用户', '活跃用户', '新增活跃'],
          textStyle: { color: colors.textSecondary, fontSize: 11 },
          top: 0,
          right: 0
        },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true, borderColor: 'transparent' },
        xAxis: {
          type: 'category',
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
        series: [
          {
            name: '原有用户',
            data: userGroup.oldData || [],
            type: 'bar',
            stack: 'users',
            barWidth: '10px',
            itemStyle: { color: colors.primary }
          },
          {
            name: '新增用户',
            data: userGroup.newData || [],
            type: 'bar',
            stack: 'users',
            barWidth: '10px',
            itemStyle: { color: '#00e676', borderRadius: [4, 4, 0, 0] }
          },
          {
            name: '活跃用户',
            data: activeGroup.oldData || [],
            type: 'bar',
            stack: 'active',
            barWidth: '10px',
            itemStyle: { color: colors.warning }
          },
          {
            name: '新增活跃',
            data: activeGroup.newData || [],
            type: 'bar',
            stack: 'active',
            barWidth: '10px',
            itemStyle: { color: '#ff6d00', borderRadius: [4, 4, 0, 0] }
          }
        ]
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.chart-card {
  @include glass-card;
  margin-bottom: 20px;
}

.chart-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgba(0, 0, 0, .05);
}

.chart-subtitle {
  display: block;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: .0625rem;
  color: $color-text-secondary;
  margin-bottom: 0.25rem;
}

.chart-title {
  margin: 0;
  font-size: 1.25rem;
  color: $color-text-primary;
}

.chart-body {
  padding: 1rem 1.5rem;
}

.chart-container {
  height: 280px;
}
</style>
