<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2 class="title">数据概览</h2>
      <GlassDropdown @command="handleTimeFilter">
        <template slot="trigger" slot-scope="{ isOpen }">
          <div class="filter-dropdown">
            <span>{{ timeFilterLabel }}</span>
            <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
          </div>
        </template>
        <GlassDropdownItem command="week">最近一周</GlassDropdownItem>
        <GlassDropdownItem command="month">最近一月</GlassDropdownItem>
        <GlassDropdownItem command="year">最近一年</GlassDropdownItem>
      </GlassDropdown>
    </div>

    <!-- 1. 顶部统计卡片 -->
    <div class="row section-container">
      <div class="col-xl-3 col-md-6" v-for="(card, index) in statCards" :key="index">
        <StatCard v-bind="card" />
      </div>
    </div>

    <!-- 2. 中间图表 -->
    <div class="row mt-4 section-container">
      <div class="col-xl-8 mb-5 mb-xl-0">
        <SalesChart />
      </div>
      <div class="col-xl-4">
        <OrdersChart />
      </div>
    </div>

    <!-- 3. 底部表格 -->
    <div class="row mt-4 section-container">
      <div class="col-xl-8 mb-5 mb-xl-0">
        <PageVisits />
      </div>
      <div class="col-xl-4">
        <SocialTraffic />
      </div>
    </div>

  </div>
</template>

<script>
import StatCard from '@/components/dashboard/StatCard.vue'
import SalesChart from '@/components/dashboard/SalesChart.vue'
import OrdersChart from '@/components/dashboard/OrdersChart.vue'
import PageVisits from '@/components/dashboard/PageVisits.vue'
import SocialTraffic from '@/components/dashboard/SocialTraffic.vue'
import GlassDropdown from '@/components/ui/GlassDropdown.vue'
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue'

export default {
  name: 'Dashboard',
  middleware: 'admin-only', // 仅管理员可访问
  components: {
    StatCard,
    SalesChart,
    OrdersChart,
    PageVisits,
    SocialTraffic,
    GlassDropdown,
    GlassDropdownItem
  },
  data() {
    return {
      timeFilter: 'month',
      allGoods: [],
      allUsers: [],
      allOrders: [],
      statCards: [
        {
          title: '物品数量',
          value: '',
          icon: 'el-icon-goods',
          iconBg: 'bg-danger',
          percentage: '',
          percentageColor: 'text-success',
          footerLabel: '较上周期'
        },
        {
          title: '用户数量',
          value: '',
          icon: 'el-icon-user',
          iconBg: 'bg-warning',
          percentage: '',
          percentageColor: 'text-success',
          footerLabel: '较上周期'
        },
        {
          title: '订单数量',
          value: '',
          icon: 'el-icon-s-order',
          iconBg: 'bg-yellow',
          percentage: '',
          percentageColor: 'text-success',
          footerLabel: '较上周期'
        },
        {
          title: '成交数量',
          value: '',
          icon: 'el-icon-s-claim',
          iconBg: 'bg-info',
          percentage: '',
          percentageColor: 'text-success',
          footerLabel: '较上周期'
        }
      ]
    }
  },
  computed: {
    timeFilterLabel() {
      const map = { week: '最近一周', month: '最近一个月', year: '最近一年' }
      return map[this.timeFilter]
    },
    periodLabel() {
      const map = { week: '较上周', month: '较上月', year: '较去年' }
      return map[this.timeFilter]
    }
  },
  created() {
    this.fetchDashboardData()
  },
  methods: {
    handleTimeFilter(command) {
      this.timeFilter = command
      this.computeStats()
      this.$root.$emit('show-island-message', `已切换数据范围：${this.timeFilterLabel}`, 'success')
    },
    getTimeRange(filter) {
      const now = new Date()
      let currentStart, prevStart, prevEnd
      if (filter === 'week') {
        currentStart = new Date(now.getTime() - 7 * 24 * 3600 * 1000)
        prevEnd = new Date(currentStart.getTime())
        prevStart = new Date(currentStart.getTime() - 7 * 24 * 3600 * 1000)
      } else if (filter === 'month') {
        currentStart = new Date(now.getFullYear(), now.getMonth() - 1, now.getDate())
        prevEnd = new Date(currentStart.getTime())
        prevStart = new Date(now.getFullYear(), now.getMonth() - 2, now.getDate())
      } else {
        currentStart = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())
        prevEnd = new Date(currentStart.getTime())
        prevStart = new Date(now.getFullYear() - 2, now.getMonth(), now.getDate())
      }
      return { currentStart, now, prevStart, prevEnd }
    },
    parseTime(item) {
      const t = item.createTime || item.createdAt
      if (!t) return null
      return new Date(t)
    },
    filterByRange(list, start, end) {
      return list.filter(item => {
        const d = this.parseTime(item)
        return d && d >= start && d <= end
      })
    },
    calcPercentage(current, previous) {
      if (previous === 0 && current === 0) return { text: '0%', color: 'text-muted' }
      if (previous === 0) return { text: '+100%', color: 'text-success' }
      const pct = ((current - previous) / previous * 100).toFixed(2)
      if (pct > 0) return { text: `+${pct}%`, color: 'text-success' }
      if (pct < 0) return { text: `${pct}%`, color: 'text-danger' }
      return { text: '0%', color: 'text-muted' }
    },
    computeStats() {
      const { currentStart, now, prevStart, prevEnd } = this.getTimeRange(this.timeFilter)
      const label = this.periodLabel

      // 物品：当前周期上架数 vs 上一周期上架数
      const curGoods = this.filterByRange(this.allGoods, currentStart, now)
        .filter(g => Number(g.shelf_status !== undefined ? g.shelf_status : g.shelfStatus) === 1)
      const prevGoods = this.filterByRange(this.allGoods, prevStart, prevEnd)
        .filter(g => Number(g.shelf_status !== undefined ? g.shelf_status : g.shelfStatus) === 1)
      const goodsPct = this.calcPercentage(curGoods.length, prevGoods.length)
      this.statCards[0].value = curGoods.length.toLocaleString()
      this.statCards[0].percentage = goodsPct.text
      this.statCards[0].percentageColor = goodsPct.color
      this.statCards[0].footerLabel = label

      // 用户：当前周期新注册非管理员用户 vs 上一周期
      const curUsers = this.filterByRange(this.allUsers, currentStart, now)
        .filter(u => u.status !== 0 && u.userType !== 1)
      const prevUsers = this.filterByRange(this.allUsers, prevStart, prevEnd)
        .filter(u => u.status !== 0 && u.userType !== 1)
      const usersPct = this.calcPercentage(curUsers.length, prevUsers.length)
      this.statCards[1].value = curUsers.length.toLocaleString()
      this.statCards[1].percentage = usersPct.text
      this.statCards[1].percentageColor = usersPct.color
      this.statCards[1].footerLabel = label

      // 订单：当前周期订单数（排除已取消） vs 上一周期
      const curOrders = this.filterByRange(this.allOrders, currentStart, now)
        .filter(o => o.status !== 'cancelled')
      const prevOrders = this.filterByRange(this.allOrders, prevStart, prevEnd)
        .filter(o => o.status !== 'cancelled')
      const ordersPct = this.calcPercentage(curOrders.length, prevOrders.length)
      this.statCards[2].value = curOrders.length.toLocaleString()
      this.statCards[2].percentage = ordersPct.text
      this.statCards[2].percentageColor = ordersPct.color
      this.statCards[2].footerLabel = label

      // 成交：当前周期已完成订单 vs 上一周期
      const curCompleted = curOrders.filter(o => o.status === 'completed')
      const prevCompleted = prevOrders.filter(o => o.status === 'completed')
      const completedPct = this.calcPercentage(curCompleted.length, prevCompleted.length)
      this.statCards[3].value = curCompleted.length.toLocaleString()
      this.statCards[3].percentage = completedPct.text
      this.statCards[3].percentageColor = completedPct.color
      this.statCards[3].footerLabel = label
    },
    async fetchDashboardData() {
      const results = await Promise.allSettled([
        this.$axios.get('/api/goods'),
        this.$axios.get('/api/user/list'),
        this.$axios.get('/api/admin/orders', { params: { pageNum: 1, pageSize: 9999 } })
      ])

      // 处理商品数据
      if (results[0].status === 'fulfilled') {
        const goodsRes = results[0].value
        if (goodsRes && (goodsRes.code === 200 || goodsRes.code === 20000 || goodsRes.code === 0)) {
          this.allGoods = goodsRes.data || []
        }
      }

      // 处理用户数据
      if (results[1].status === 'fulfilled') {
        const usersRes = results[1].value
        if (usersRes && (usersRes.code === 200 || usersRes.code === 20000 || usersRes.code === 0)) {
          this.allUsers = usersRes.data || []
        }
      }

      // 处理订单数据
      if (results[2].status === 'fulfilled') {
        const ordersRes = results[2].value
        if (ordersRes && (ordersRes.code === 200 || ordersRes.code === 20000 || ordersRes.code === 0)) {
          const pageData = ordersRes.data || {}
          this.allOrders = pageData.records || []
        }
      }

      this.computeStats()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.dashboard-page {
  padding-bottom: 40px;
}

// 优化渲染性能
.section-container {
  transform: translateZ(0);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .title {
    font-size: 24px;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  .filter-dropdown {
    @include glass-effect;
    padding: 10px 20px;
    border-radius: 12px;
    cursor: pointer;
    font-size: 14px;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon-arrow-down {
      transition: transform 0.3s;
      &.rotate-180 {
        transform: rotate(180deg);
      }
    }
  }
}

// Simple grid system
.row {
  display: flex;
  flex-wrap: wrap;
  margin-right: -10px;
  margin-left: -10px;
}

.col-xl-3, .col-xl-8, .col-xl-4, .col-md-6 {
  position: relative;
  width: 100%;
  padding-right: 10px;
  padding-left: 10px;
}

@media (min-width: 768px) {
  .col-md-6 {
    flex: 0 0 50%;
    max-width: 50%;
  }
}

@media (min-width: 1200px) {
  .col-xl-3 {
    flex: 0 0 25%;
    max-width: 25%;
  }
  .col-xl-4 {
    flex: 0 0 33.333333%;
    max-width: 33.333333%;
  }
  .col-xl-8 {
    flex: 0 0 66.666667%;
    max-width: 66.666667%;
  }
  .mb-xl-0 {
    margin-bottom: 0 !important;
  }
}

.mt-4 {
  margin-top: 1rem !important;
}

.mb-5 {
  margin-bottom: 1.5rem !important;
}
</style>
