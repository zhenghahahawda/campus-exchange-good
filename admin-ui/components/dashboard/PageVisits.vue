<template>
  <div class="card shadow">
    <div class="card-header border-0">
      <div class="row align-items-center">
        <div class="col">
          <h3 class="mb-0">商品类型销售统计</h3>
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
            <th scope="col">商品类型</th>
            <th scope="col">上架数量</th>
            <th scope="col">售出数量</th>
            <th scope="col">售出率</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in tableData" :key="index">
            <th scope="row">
              {{ item.categoryName }}
            </th>
            <td>
              {{ item.onShelfCount }}
            </td>
            <td>
              {{ item.soldCount }}
            </td>
            <td>
              <div class="d-flex align-items-center">
                <span class="mr-2">{{ item.sellRate }}</span>
                <div>
                  <div class="progress">
                    <div
                      class="progress-bar"
                      role="progressbar"
                      :aria-valuenow="item.sellRateNum"
                      aria-valuemin="0"
                      aria-valuemax="100"
                      :style="{ width: item.sellRateNum + '%' }"
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
  name: 'PageVisits',
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
      const results = await Promise.allSettled([
        this.$axios.get('/api/goods'),
        this.$axios.get('/api/categories/active')
      ]);

      // 处理商品数据
      let goods = [];
      if (results[0].status === 'fulfilled') {
        const goodsResponse = results[0].value;
        goods = (goodsResponse && (goodsResponse.code === 20000 || goodsResponse.code === 200 || goodsResponse.code === 0))
          ? goodsResponse.data
          : [];
      }

      // 处理分类数据
      let categories = [];
      if (results[1].status === 'fulfilled') {
        const categoriesResponse = results[1].value;
        categories = (categoriesResponse && (categoriesResponse.code === 20000 || categoriesResponse.code === 200 || categoriesResponse.code === 0))
          ? categoriesResponse.data
          : [];
      }

      this.processData(goods, categories);
      this.loading = false;
    },
    processData(goods, categories) {
      // 1. 初始化分类统计 Map
      const categoryStats = {};
      categories.forEach(cat => {
        // 注意：这里用 categoryId 作为 key
        categoryStats[cat.categoryId] = {
          name: cat.categoryName,
          total: 0,
          sold: 0
        };
      });

      // 2. 遍历商品进行统计
      goods.forEach(item => {
        // 尝试获取分类标识 (可能是 ID 或 名称)
        // 后端可能返回 categoryId, category_id, 或 category (可能是对象、ID或字符串)
        let rawCategory = item.categoryId || item.category_id || item.category;
        
        // 如果是对象，尝试获取 id
        if (typeof rawCategory === 'object' && rawCategory !== null) {
            rawCategory = rawCategory.id || rawCategory.categoryId;
        }

        let stat = null;

        // 尝试 1: 直接作为 ID 匹配
        if (categoryStats[rawCategory]) {
          stat = categoryStats[rawCategory];
        } 
        // 尝试 2: 作为名称匹配
        else {
           const foundCat = categories.find(c => 
             c.categoryName === rawCategory || 
             c.categoryCode === rawCategory ||
             String(c.categoryId) === String(rawCategory) // 尝试字符串ID匹配
           );
           if (foundCat) {
             stat = categoryStats[foundCat.categoryId];
           }
        }

        if (stat) {
          const shelfStatus = Number(item.shelf_status !== undefined ? item.shelf_status : item.shelfStatus);
          // shelf_status: 0=已下架, 1=已上架, 2=售出
          if (shelfStatus === 1) {
            stat.total++;
          } else if (shelfStatus === 2) {
            stat.total++;
            stat.sold++;
          }
        }
      });

      // 3. 转换为数组并计算售出率
      const colors = [
        'bg-gradient-primary',
        'bg-gradient-success',
        'bg-gradient-info',
        'bg-gradient-warning',
        'bg-gradient-danger'
      ];

      this.tableData = Object.values(categoryStats)
        .map((stat, index) => {
          const rate = stat.total > 0 ? (stat.sold / stat.total * 100) : 0;
          return {
            categoryName: stat.name,
            onShelfCount: stat.total,
            soldCount: stat.sold,
            sellRate: rate.toFixed(2) + '%',
            sellRateNum: rate, // 添加数值用于进度条
            colorClass: colors[index % colors.length] // 添加颜色类
          };
        })
        .sort((a, b) => b.onShelfCount - a.onShelfCount) // 按上架数量降序
        .slice(0, 5); // 取前5
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
      color: $color-text-primary; // 标题颜色
    }
  }

  .table {
    width: 100%;
    margin-bottom: 1rem;
    color: $color-text-secondary; // 表格内容文字颜色
    background-color: transparent;
    
    th, td {
      padding: 1rem;
      vertical-align: middle; // 垂直居中
      border-top: 1px solid rgba(255, 255, 255, 0.1); // 半透明白色边框
      font-weight: $font-weight-medium;
    }
    
    thead th {
      vertical-align: bottom;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      color: $color-text-secondary; // 表头颜色
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
  
  &.btn-light-pill {
    background-color: #F1F4FA;
    color: #8A8A8A;
    border-radius: 9999px;
    padding: 0.5rem 1.2rem; // 增加左右内边距以适应 pill 形状
    border: none;
    box-shadow: none;
    font-weight: 600;
    
    &:hover {
      background-color: darken(#F1F4FA, 3%);
      color: #5e6e82;
      transform: translateY(-1px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

.text-success {
  color: #2dce89;
}

.text-warning {
  color: #fb6340;
}

.mr-3 {
  margin-right: 1rem;
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
    opacity: 0.8;
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