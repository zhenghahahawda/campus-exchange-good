<template>
  <div class="login-log-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader title="系统登录日志" description="查看与管理用户系统登录记录" />
      <div class="header-actions" v-if="selectedLogIds.length > 0">
        <button class="glass-btn btn-danger" @click="handleBatchDelete">
          <i class="el-icon-delete"></i>
          <span>批量删除</span>
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="row section-container">
      <div class="col-xl-4 col-md-6" v-for="(card, index) in statCardsData" :key="index">
        <StatCard 
          v-bind="card" 
          @click.native="handleStatClick(card.status)" 
          :class="{ 'active-stat-card': filterStatus === card.status }" 
        />
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <el-input
          placeholder="搜索用户名/IP/浏览器/操作系统..."
          v-model="searchQuery"
          prefix-icon="el-icon-search"
          clearable
          class="capsule-input"
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
        />
      </div>
      
      <div class="sort-wrapper">
        <GlassDropdown @command="handleSortCommand">
          <template slot="trigger" slot-scope="{ isOpen }">
            <div class="filter-dropdown">
              <span>{{ getSortLabel(sortBy) }}</span>
              <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
            </div>
          </template>
          <GlassDropdownItem 
            v-for="(label, key) in LOGIN_LOG_SORT_LABELS" 
            :key="key"
            :command="key"
          >
            {{ label }}
          </GlassDropdownItem>
        </GlassDropdown>
      </div>
    </div>

    <!-- 日志表格 -->
    <div v-if="logsList.length > 0" class="log-table-wrapper glass-card">
      <el-table 
        :data="logsList" 
        style="width: 100%" 
        class="glass-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="logId" label="日志ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="loginIp" label="登录IP" width="140" show-overflow-tooltip />
        <el-table-column label="登录状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusColor(scope.row.loginStatus)" size="small">
              {{ getStatusLabel(scope.row.loginStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userAgent" label="User Agent" min-width="150" show-overflow-tooltip />
        <el-table-column prop="deviceType" label="设备" width="100" />
        <el-table-column prop="browser" label="浏览器" width="120" />
        <el-table-column prop="os" label="系统" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              icon="el-icon-delete" 
              class="text-danger" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <EmptyState 
      v-else 
      icon="el-icon-monitor" 
      message="暂无符合条件的登录日志" 
      action-text="清除筛选条件" 
      :show-action="true"
      @action="handleClearFilters" 
    />

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="PAGINATION_CONFIG.PAGE_SIZES"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="glass-pagination"
      />
    </div>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';
import StatCard from '@/components/dashboard/StatCard.vue';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';

import { 
  LoginLogStats
} from '@/utils/loginLogManager';
import { 
  LOGIN_STATUS, 
  LOGIN_STATUS_LABELS, 
  LOGIN_STATUS_COLORS,
  LOGIN_LOG_SORT_LABELS,
  LOGIN_LOG_SORT_OPTIONS,
  PAGINATION_CONFIG
} from '@/utils/loginLogConstants';

export default {
  name: 'LoginLogPage',
  middleware: 'admin-only',
  components: {
    PageHeader,
    EmptyState,
    GlassDropdown,
    GlassDropdownItem,
    StatCard
  },
  
  data() {
    return {
      logsList: [],
      total: 0,
      searchQuery: '',
      filterStatus: LOGIN_STATUS.ALL,
      sortBy: LOGIN_LOG_SORT_OPTIONS.LATEST,
      currentPage: PAGINATION_CONFIG.DEFAULT_PAGE,
      pageSize: 20, // 默认每页20条
      selectedLogIds: [],
      statCardsData: [],

      LOGIN_LOG_SORT_LABELS,
      PAGINATION_CONFIG
    };
  },

  computed: {
  },

  created() {
    this.fetchStats();
    this.fetchLogs();
  },

  watch: {
    searchQuery(newVal) {
      if (!newVal) {
        this.handleSearch();
      }
    }
  },

  methods: {
    // 获取全局统计数据
    async fetchStats() {
      try {
        const response = await this.$axios.get('/api/login-logs/stats');
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const stats = response.data || {};
          this.statCardsData = [
            {
              title: '总登录次数',
              value: String(stats.total || 0),
              icon: 'el-icon-monitor',
              iconBg: 'bg-primary',
              footerLabel: '平台累计登录次数',
              status: LOGIN_STATUS.ALL
            },
            {
              title: '登录成功',
              value: String(stats.success || 0),
              icon: 'el-icon-circle-check',
              iconBg: 'bg-success',
              footerLabel: '登录成功记录',
              status: LOGIN_STATUS.SUCCESS
            },
            {
              title: '登录失败',
              value: String(stats.failed || 0),
              icon: 'el-icon-circle-close',
              iconBg: 'bg-danger',
              footerLabel: '异常登录尝试',
              status: LOGIN_STATUS.FAILED
            }
          ];
        }
      } catch (error) {
        console.error('Fetch stats error:', error);
      }
    },

    // 获取日志列表
    async fetchLogs() {
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchQuery,
          sort: this.sortBy
        };

        if (this.filterStatus !== LOGIN_STATUS.ALL) {
          params.status = this.filterStatus;
        }

        const response = await this.$axios.get('/api/login-logs/page', { params });
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const data = response.data || {};
          // 兼容不同的分页数据结构
          this.logsList = data.records || data.list || [];
          this.total = data.total || 0;
        } else {
          MessageHelper.error(this, response.message || '获取日志列表失败');
        }
      } catch (error) {
        console.error('Fetch logs error:', error);
        MessageHelper.error(this, '获取日志列表失败');
      }
    },

    getStatusLabel(status) {
      return LOGIN_STATUS_LABELS[status];
    },

    getStatusColor(status) {
      return LOGIN_STATUS_COLORS[status];
    },

    getSortLabel(sortOption) {
      return LOGIN_LOG_SORT_LABELS[sortOption];
    },

    handleSortCommand(command) {
      this.sortBy = command;
      this.currentPage = 1;
      this.fetchLogs();
    },

    handleStatClick(status) {
      this.filterStatus = status;
      this.currentPage = 1;
      this.fetchLogs();
    },

    handleSearch() {
      this.currentPage = 1;
      this.fetchLogs();
    },

    handlePageChange(page) {
      this.currentPage = page;
      this.fetchLogs();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.fetchLogs();
    },

    handleClearFilters() {
      this.searchQuery = '';
      this.filterStatus = LOGIN_STATUS.ALL;
      this.sortBy = LOGIN_LOG_SORT_OPTIONS.LATEST;
      this.currentPage = 1;
      this.fetchLogs();
    },

    handleSelectionChange(selection) {
      this.selectedLogIds = selection.map(item => item.logId);
    },

    async handleDelete(log) {
      try {
        await ConfirmHelper.confirmDelete(this, `日志 ID: ${log.logId}`);
        
        const response = await this.$axios.delete(`/api/login-logs/${log.logId}`);
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '删除成功');
          // 如果当前页只有一条数据且不是第一页，则跳转到上一页
          if (this.logsList.length === 1 && this.currentPage > 1) {
            this.currentPage--;
          }
          this.fetchStats();
          this.fetchLogs();
        } else {
          MessageHelper.error(this, response.message || '删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(this, '删除操作异常');
        }
      }
    },

    async handleBatchDelete() {
      if (this.selectedLogIds.length === 0) return;
      
      try {
        await ConfirmHelper.confirmDelete(this, `选中的 ${this.selectedLogIds.length} 条日志`);
        
        const response = await this.$axios.delete('/api/login-logs/batch', {
          data: this.selectedLogIds
        });
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '批量删除成功');
          this.selectedLogIds = [];
          this.fetchStats();
          this.fetchLogs();
        } else {
          MessageHelper.error(this, response.message || '批量删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(this, '批量删除操作异常');
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.login-log-page {
  padding: 10px 20px;
  min-height: 100vh;
  color: $color-text-primary;
}

.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 0px;
  
  .header-actions {
    display: flex;
    gap: 12px;
  }

  .btn-danger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    background: rgba($color-secondary-danger, 0.1);
    border: 1px solid rgba($color-secondary-danger, 0.3);
    color: $color-secondary-danger;
    transition: all 0.3s ease;

    &:hover {
      background: $color-secondary-danger;
      color: #fff;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba($color-secondary-danger, 0.3);
    }
  }
}

.section-container {
  margin-bottom: 32px;
}

.row {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -10px;
}

.col-xl-4, .col-md-6 {
  width: 100%;
  padding: 0 10px;
  cursor: pointer;
}

@media (min-width: 768px) {
  .col-md-6 { flex: 0 0 50%; max-width: 50%; }
}

@media (min-width: 1200px) {
  .col-xl-4 { flex: 0 0 33.333%; max-width: 33.333%; }
}

.active-stat-card {
  transform: translateY(-4px) !important;
  box-shadow: 0 8px 25px rgba(var(--color-primary-rgb), 0.25) !important;
  border: 2px solid rgba(var(--color-primary-rgb), 0.6) !important;
  background: rgba(var(--color-primary-rgb), 0.08) !important;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 40%;
    height: 3px;
    background: linear-gradient(90deg, transparent, rgba(var(--color-primary-rgb), 0.8), transparent);
    border-radius: 3px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .search-wrapper {
    flex: 1;
    max-width: 400px;
  }
  
  .filter-dropdown {
    @include glass-effect;
    padding: 10px 20px;
    border-radius: 50px;
    cursor: pointer;
    font-size: 14px;
    color: $color-text-secondary;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.3s ease;
    
    &:hover {
      color: $color-primary;
      background: var(--color-bg-surface);
    }
    
    i.rotate-180 { transform: rotate(180deg); }
  }
}

.log-table-wrapper {
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 24px;
  @include glass-card;
}

.glass-table {
  background: transparent !important;
  
  ::v-deep {
    th.el-table__cell {
      background: rgba(255, 255, 255, 0.05) !important;
      color: $color-text-primary !important;
      font-weight: 600;
      border-bottom: 1px solid $color-border !important;
    }
    
    td.el-table__cell {
      background: transparent !important;
      color: $color-text-secondary !important;
      border-bottom: 1px solid $color-border !important;
    }
    
    .el-table__row:hover > td.el-table__cell {
      background: rgba(var(--color-primary-rgb), 0.05) !important;
    }

    // 强制去除整个表格的背景色（包括空数据区域）
    &.el-table {
      background-color: transparent !important;
    }

    // 强制去除表头和表尾的背景色
    .el-table__header-wrapper, 
    .el-table__footer-wrapper {
      background-color: transparent !important;
    }

    // 强制去除表头行背景
    .el-table__header tr {
        background-color: transparent !important;
    }

    // 去除行背景
    .el-table__row {
      background-color: transparent !important;
    }

    // 去除空状态背景
    .el-table__empty-block {
      background-color: transparent !important;
    }
    .el-table__empty-text {
      color: $color-text-disabled !important;
    }
    
    // 强制去除底部边框
    &::before {
      display: none;
    }

    // 自定义状态标签样式
    .el-tag {
      border: none;
      border-radius: 20px;
      padding: 0 16px;
      height: 28px;
      line-height: 28px;
      font-weight: 500;
      font-size: 12px;
      
      &.el-tag--success {
        background-color: rgba(var(--color-secondary-success-rgb), 0.15);
        color: var(--color-secondary-success);
      }
      
      &.el-tag--info {
        background-color: rgba(var(--color-secondary-info-rgb), 0.15);
        color: var(--color-secondary-info);
      }
      
      &.el-tag--warning {
        background-color: rgba(var(--color-secondary-warning-rgb), 0.15);
        color: var(--color-secondary-warning);
      }
      
      &.el-tag--danger {
        background-color: rgba(var(--color-secondary-danger-rgb), 0.15);
        color: var(--color-secondary-danger);
      }
    }
  }
}

.text-danger {
  color: #ff4d4f;
  &:hover { color: #ff7875; }
}

::v-deep {
  .capsule-input .el-input__inner {
    border-radius: 50px;
    background: var(--color-pill-bg);
    border: 1px solid rgba(255, 255, 255, 0.1);
    height: 46px;
    color: $color-text-primary;
    
    &:focus {
      border-color: $color-primary;
      background: var(--color-bg-surface);
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@import '@/assets/css/components/pagination.scss';
</style>
