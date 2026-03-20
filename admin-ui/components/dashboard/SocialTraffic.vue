<template>
  <div class="card shadow">
    <div class="card-header border-0">
      <div class="row align-items-center">
        <div class="col">
          <h3 class="mb-0">用户来源</h3>
        </div>
        <div class="col text-right">
          <GlassDropdown @command="handleCommand">
            <template slot="trigger" slot-scope="{ isOpen }">
              <div class="filter-dropdown">
                <span>查看全部</span>
                <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
              </div>
            </template>
            <GlassDropdownItem command="all">查看全部</GlassDropdownItem>
            <GlassDropdownItem command="export">导出报表</GlassDropdownItem>
          </GlassDropdown>
        </div>
      </div>
    </div>
    <div class="table-responsive">
      <table class="table align-items-center table-flush">
        <thead class="thead-light">
          <tr>
            <th scope="col">来源</th>
            <th scope="col">人数</th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in tableData" :key="index">
            <th scope="row">
              {{ item.referral }}
            </th>
            <td>
              {{ item.visitors }}
            </td>
            <td>
              <div class="d-flex align-items-center">
                <span class="mr-2">{{ item.percentage }}%</span>
                <div>
                  <div class="progress">
                    <div 
                      class="progress-bar" 
                      role="progressbar" 
                      :aria-valuenow="item.percentage" 
                      aria-valuemin="0" 
                      aria-valuemax="100" 
                      :style="{ width: item.percentage + '%' }"
                      :class="item.colorClass"
                    ></div>
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue'
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue'

export default {
  name: 'SocialTraffic',
  components: {
    GlassDropdown,
    GlassDropdownItem
  },
  data() {
    return {
      tableData: [],
      loading: false
    }
  },
  async created() {
    await this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      const result = await Promise.allSettled([
        this.$axios.get('/api/user/list')
      ]);

      if (result[0].status === 'fulfilled') {
        const response = result[0].value;
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const users = response.data || [];
          this.processData(users);
        }
      }

      this.loading = false;
    },
    processData(users) {
      // 统计各学校人数
      const schoolStats = {};
      let totalUsers = 0;

      users.forEach(user => {
        if (user.school) {
          schoolStats[user.school] = (schoolStats[user.school] || 0) + 1;
          totalUsers++;
        }
      });

      // 转换为数组并排序
      const sortedStats = Object.entries(schoolStats)
        .map(([school, count]) => ({
          referral: school,
          visitors: count.toLocaleString(),
          percentage: totalUsers > 0 ? Math.round((count / totalUsers) * 100) : 0,
          rawCount: count
        }))
        .sort((a, b) => b.rawCount - a.rawCount)
        .slice(0, 5); // 只取前5名

      // 如果数据不足5条，可以用“其他”补齐或者只显示实际数据
      // 这里为了美观，我们动态分配颜色
      const colors = [
        'bg-gradient-primary',
        'bg-gradient-success',
        'bg-gradient-info',
        'bg-gradient-warning',
        'bg-gradient-danger'
      ];

      this.tableData = sortedStats.map((item, index) => ({
        ...item,
        colorClass: colors[index % colors.length]
      }));
    },
    handleCommand(command) {
      if (command === 'all') {
        // Handle view all
        console.log('View all clicked');
      } else if (command === 'export') {
        // Handle export
        console.log('Export clicked');
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
  @include glass-card;
  margin-bottom: 30px;
}

.shadow {
  box-shadow: 0 0 2rem 0 rgba(136, 152, 170, .15);
}

.card-header {
    padding: 1.25rem 1.5rem;
    margin-bottom: 0;
    background-color: transparent;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1); // 使用主题边框色
    
    h3 {
      color: $color-text-primary;
    }
  }

  .table {
    width: 100%;
    margin-bottom: 1rem;
    color: $color-text-secondary; // 表格内容文字颜色
    background-color: transparent;
    
    th, td {
      padding: 1rem;
      vertical-align: middle;
      border-top: 1px solid rgba(255, 255, 255, 0.1); // 半透明白色边框
      font-weight: $font-weight-medium;
    }
    
    thead th {
      vertical-align: bottom;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      color: $color-text-secondary;
      background-color: rgba(255, 255, 255, 0.05); // 表头微弱背景
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    
    // 首列强调
    th[scope="row"] {
      color: $color-text-primary;
      font-weight: $font-weight-semibold;
    }
  }

.align-items-center {
  align-items: center;
}

.table-flush {
  td, th {
    border-right: 0;
    border-left: 0;
  }
  
  tbody tr:first-child td,
  tbody tr:first-child th {
    border-top: 0;
  }
}

.thead-light {
    th {
      color: $color-text-secondary;
      background-color: transparent;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
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

.filter-dropdown {
    @include glass-effect;
    padding: 6px 16px;
    border-radius: 9999px;
    cursor: pointer;
    font-size: 0.875rem;
    font-weight: 600;
    color: $color-text-secondary; // 使用主题变量
    display: inline-flex;
    align-items: center;
    gap: 8px;
    
    &:hover {
      color: $color-text-primary; // 悬停变亮
      background: rgba(255, 255, 255, 0.1);
    }
    
    .el-icon-arrow-down {
      transition: transform 0.3s;
      font-size: 12px;
      
      &.rotate-180 {
        transform: rotate(180deg);
      }
    }
  }

.text-right {
  text-align: right;
}

.btn {
  display: inline-block;
  font-weight: 600;
  text-align: center;
  vertical-align: middle;
  user-select: none;
  border: 1px solid transparent;
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  transition: all 0.15s ease;
  
  &.btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    line-height: 1.5;
    border-radius: 0.25rem;
  }
  
  &.btn-primary {
    @include glass-button;
  }
}

.d-flex {
  display: flex;
}

.mr-2 {
  margin-right: 0.5rem;
}

.progress {
    display: flex;
    height: 0.5rem;
    overflow: hidden;
    font-size: .75rem;
    background-color: rgba(255, 255, 255, 0.1); // 进度条背景改为半透明白
    border-radius: .25rem;
    width: 120px;
  }

.progress-bar {
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  text-align: center;
  white-space: nowrap;
  background-color: $color-primary;
  transition: width 0.6s ease;
  border-radius: 999px;
  
  // 主题色渐变
  &.bg-gradient-primary {
    background: $color-primary;
    box-shadow: 0 2px 10px rgba($color-primary-rgb, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    opacity: 0.8; // 增加一点透明度实现薄雾感
    backdrop-filter: blur(4px);
  }
  
  &.bg-gradient-success {
    background: $color-secondary-success;
    box-shadow: 0 2px 10px rgba($color-secondary-success-rgb, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    opacity: 0.8;
    backdrop-filter: blur(4px);
  }
  
  &.bg-gradient-info {
    background: $color-secondary-info;
    box-shadow: 0 2px 10px rgba($color-secondary-info-rgb, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    opacity: 0.8;
    backdrop-filter: blur(4px);
  }
  
  &.bg-gradient-warning {
    background: $color-secondary-warning;
    box-shadow: 0 2px 10px rgba($color-secondary-warning-rgb, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    opacity: 0.8;
    backdrop-filter: blur(4px);
  }
  
  &.bg-gradient-danger {
    background: $color-secondary-danger;
    box-shadow: 0 2px 10px rgba($color-secondary-danger-rgb, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);
    opacity: 0.8;
    backdrop-filter: blur(4px);
  }
}
</style>