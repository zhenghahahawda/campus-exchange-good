<template>
  <div class="session-manage-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader title="消息管理" description="管理用户会话信息 (user_sessions)" />
      <div class="header-actions">
        <button class="glass-btn btn-warning" @click="handleCleanExpired">
          <i class="el-icon-brush"></i>
          <span>清理过期</span>
        </button>
        <button v-if="selectedIds.length > 0" class="glass-btn btn-warning" @click="handleBatchDeactivate">
          <i class="el-icon-switch-button"></i>
          <span>一键下线</span>
        </button>
        <button v-if="selectedIds.length > 0" class="glass-btn btn-danger" @click="handleBatchDelete">
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
          placeholder="搜索会话ID/用户ID/IP/设备标识..."
          v-model="searchQuery"
          prefix-icon="el-icon-search"
          clearable
          class="capsule-input"
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
        />
      </div>
      
      <div class="sort-wrapper">
        <el-button type="primary" icon="el-icon-refresh" circle @click="fetchSessions" class="glass-btn-icon"></el-button>
      </div>
    </div>

    <!-- 会话表格 -->
    <div v-if="sessionList.length > 0" class="session-table-wrapper glass-card">
      <el-table 
        :data="sessionList" 
        style="width: 100%" 
        class="glass-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="sessionId" label="会话ID" width="120" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="deviceId" label="设备标识" width="100" show-overflow-tooltip />
        <el-table-column prop="userAgent" label="用户代理" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column prop="lastActivityAt" label="最后活跃" width="160" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusColor(scope.row.isActive)" size="small">
              {{ getStatusLabel(scope.row.isActive) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.isActive === 1"
              type="text" 
              icon="el-icon-switch-button" 
              class="text-warning" 
              @click="handleDeactivate(scope.row)"
            >
              下线
            </el-button>
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
      icon="el-icon-chat-dot-square" 
      message="暂无符合条件的会话信息" 
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
import StatCard from '@/components/dashboard/StatCard.vue';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';

import { 
  SessionStats 
} from '@/utils/sessionManager';
import { 
  SESSION_STATUS, 
  SESSION_STATUS_LABELS, 
  SESSION_STATUS_COLORS,
  PAGINATION_CONFIG
} from '@/utils/sessionConstants';

export default {
  name: 'SessionManagePage',
  middleware: 'admin-only',
  components: {
    PageHeader,
    EmptyState,
    StatCard
  },
  
  data() {
    return {
      sessionList: [],
      total: 0,
      searchQuery: '',
      filterStatus: 'all',
      currentPage: PAGINATION_CONFIG.DEFAULT_PAGE,
      pageSize: 20,
      selectedIds: [],
      selectedRows: [],
      statCardsData: [],

      PAGINATION_CONFIG
    };
  },

  computed: {
  },

  created() {
    this.fetchStats();
    this.fetchSessions();
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
        const response = await this.$axios.get('/api/sessions/stats');
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const stats = response.data || {};
          this.statCardsData = [
            { title: '总会话', value: String(stats.total || 0), icon: 'el-icon-monitor', color: 'primary', status: 'all' },
            { title: '活跃会话', value: String(stats.active || 0), icon: 'el-icon-circle-check', color: 'success', status: SESSION_STATUS.ACTIVE },
            { title: '已失效', value: String(stats.inactive || 0), icon: 'el-icon-circle-close', color: 'info', status: SESSION_STATUS.INACTIVE }
          ];
        }
      } catch (error) {
        console.error('Fetch stats error:', error);
      }
    },

    // 获取会话列表
    async fetchSessions() {
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchQuery
        };
        
        if (this.filterStatus !== 'all') {
          params.isActive = this.filterStatus;
        }

        const response = await this.$axios.get('/api/sessions/page', { params });
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const data = response.data || {};
          this.sessionList = data.records || data.list || [];
          this.total = data.total || 0;
        } else {
           MessageHelper.error(this, response.message || '获取会话列表失败');
        }
      } catch (error) {
        console.error('Fetch sessions error:', error);
        MessageHelper.error(this, '获取会话列表失败');
      }
    },

    getStatusLabel(status) {
      return SESSION_STATUS_LABELS[status] || '未知';
    },

    getStatusColor(status) {
      return SESSION_STATUS_COLORS[status] || 'info';
    },

    handleStatClick(status) {
      this.filterStatus = status;
      this.currentPage = 1;
      this.fetchSessions();
    },

    handleSearch() {
      this.currentPage = 1;
      this.fetchSessions();
    },

    handlePageChange(page) {
      this.currentPage = page;
      this.fetchSessions();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.fetchSessions();
    },

    handleClearFilters() {
      this.searchQuery = '';
      this.filterStatus = 'all';
      this.currentPage = 1;
      this.fetchSessions();
    },

    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.sessionId);
      this.selectedRows = selection;
    },

    async handleDeactivate(row) {
      try {
        await ConfirmHelper.confirm(this, '确认强制下线该会话吗？', `会话 ID: ${row.sessionId}`);

        const response = await this.$axios.post(`/api/sessions/${row.sessionId}/deactivate`);

        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '强制下线成功');
          this.fetchStats();
          this.fetchSessions();
        } else {
          MessageHelper.error(this, response.message || '强制下线失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(this, '操作异常');
        }
      }
    },

    async handleBatchDeactivate() {
      const activeIds = this.selectedRows
        .filter(row => row.isActive === 1)
        .map(row => row.sessionId);

      if (activeIds.length === 0) {
        MessageHelper.error(this, '选中的会话中没有活跃会话');
        return;
      }

      try {
        await ConfirmHelper.confirm(this, `确认批量下线选中的 ${activeIds.length} 个活跃会话吗？`);

        const response = await this.$axios.post('/api/sessions/batch/deactivate', activeIds);

        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, `成功下线 ${activeIds.length} 个会话`);
          this.selectedIds = [];
          this.selectedRows = [];
          this.fetchStats();
          this.fetchSessions();
        } else {
          MessageHelper.error(this, response.message || '批量下线失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(this, '批量下线操作异常');
        }
      }
    },

    async handleCleanExpired() {
      try {
        await ConfirmHelper.confirm(this, '确认清理所有过期会话吗？这将删除所有状态为已失效的会话记录。');
        
        // 开启加载状态
        const loading = this.$loading({
          lock: true,
          text: '正在清理过期会话...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });

        try {
          // 改回 POST 请求，通常动作类接口使用 POST
          const response = await this.$axios.post('/api/sessions/clean-expired');
          
          loading.close();

          // 兼容不同的成功状态码
          if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
            const count = response.data || 0;
            MessageHelper.success(this, `清理成功，共清理 ${count} 条过期会话`);
            // 重新获取列表以刷新数据
            this.currentPage = 1;
            this.fetchStats();
            this.fetchSessions();
          } else {
            MessageHelper.error(this, response.message || '清理失败');
          }
        } catch (err) {
          loading.close();
          throw err;
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(error);
          MessageHelper.error(this, '操作异常');
        }
      }
    },

    async handleDelete(row) {
      try {
        await ConfirmHelper.confirmDelete(this, `会话 ID: ${row.sessionId}`);
        
        const response = await this.$axios.delete(`/api/sessions/${row.sessionId}`);
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '删除成功');
          if (this.sessionList.length === 1 && this.currentPage > 1) {
            this.currentPage--;
          }
          this.fetchStats();
          this.fetchSessions();
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
      if (this.selectedIds.length === 0) return;
      
      try {
        await ConfirmHelper.confirmDelete(this, `选中的 ${this.selectedIds.length} 个会话`);
        
        const response = await this.$axios.delete('/api/sessions/batch', {
          data: this.selectedIds
        });
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '批量删除成功');
          this.selectedIds = [];
          this.fetchStats();
          this.fetchSessions();
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

.session-manage-page {
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
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    background: linear-gradient(135deg, 
      rgba(var(--color-secondary-danger-rgb), 0.95), 
      rgba(var(--color-secondary-danger-rgb), 0.85));
    border: 1px solid rgba(var(--color-secondary-danger-rgb), 0.3);
    color: #fff;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(var(--color-secondary-danger-rgb), 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;

    i {
      font-size: 16px;
      font-weight: bold;
    }

    &:hover {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-danger-rgb), 1), 
        rgba(var(--color-secondary-danger-rgb), 0.9));
      border-color: rgba(var(--color-secondary-danger-rgb), 0.5);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-secondary-danger-rgb), 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }

  .btn-warning {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    background: linear-gradient(135deg, 
      rgba(var(--color-secondary-warning-rgb), 0.95), 
      rgba(var(--color-secondary-warning-rgb), 0.85));
    border: 1px solid rgba(var(--color-secondary-warning-rgb), 0.3);
    color: #fff;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(var(--color-secondary-warning-rgb), 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;

    i {
      font-size: 16px;
      font-weight: bold;
    }

    &:hover {
      background: linear-gradient(135deg, 
        rgba(var(--color-secondary-warning-rgb), 1), 
        rgba(var(--color-secondary-warning-rgb), 0.9));
      border-color: rgba(var(--color-secondary-warning-rgb), 0.5);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-secondary-warning-rgb), 0.4);
    }
    
    &:active {
      transform: translateY(0);
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

  .glass-btn-icon {
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: $color-text-primary;
    
    &:hover {
      background: $color-primary;
      border-color: $color-primary;
      color: #fff;
    }
  }
}

.session-table-wrapper {
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

    // 强制去除整个表格的背景色
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

.text-warning {
  color: #faad14;
  &:hover { color: #ffc53d; }
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
