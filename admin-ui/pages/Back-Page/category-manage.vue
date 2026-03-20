<template>
  <div class="category-manage-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader title="商品分类管理" description="管理商品分类信息" />
      <div class="header-actions">
        <button class="glass-btn btn-primary" @click="handleAdd">
          <i class="el-icon-plus"></i>
          <span>新增分类</span>
        </button>
        <button v-if="selectedIds.length > 0" class="glass-btn btn-danger" @click="handleBatchDelete">
          <i class="el-icon-delete"></i>
          <span>批量删除</span>
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="row section-container">
      <div
        class="col-xl-4 col-md-6"
        v-for="(card, index) in statCardsData"
        :key="index"
        @click="handleStatClick(card.status)"
      >
        <StatCard
          v-bind="card"
          :class="{ 'active-stat-card': filterStatus === card.status }"
        />
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar">
      <div class="search-wrapper">
        <el-input
          placeholder="搜索分类名称/代码..."
          v-model="searchQuery"
          prefix-icon="el-icon-search"
          clearable
          class="capsule-input"
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
        />
      </div>
      
      <div class="sort-wrapper">
        <el-button type="primary" icon="el-icon-refresh" circle @click="fetchCategories" class="glass-btn-icon"></el-button>
      </div>
    </div>

    <!-- 分类表格 -->
    <div v-if="categoryList.length > 0" class="category-table-wrapper glass-card">
      <el-table 
        :data="categoryList" 
        style="width: 100%" 
        class="glass-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column prop="categoryId" label="分类ID" width="100" sortable />
        <el-table-column prop="categoryName" label="分类名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="categoryCode" label="分类代码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" sortable />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusColor(scope.row.isActive)" size="small">
              {{ getStatusLabel(scope.row.isActive) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              :icon="scope.row.isActive === 1 ? 'el-icon-switch-button' : 'el-icon-video-play'" 
              :class="scope.row.isActive === 1 ? 'text-warning' : 'text-success'" 
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.isActive === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              type="text" 
              icon="el-icon-edit" 
              class="text-primary" 
              @click="handleEdit(scope.row)"
            >
              编辑
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
      icon="el-icon-collection-tag" 
      message="暂无符合条件的分类信息" 
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      custom-class="glass-dialog"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="80px" class="glass-form">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类代码" prop="categoryCode">
          <el-input v-model="form.categoryCode" placeholder="请输入唯一分类代码" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input v-model.number="form.sortOrder" type="number" placeholder="请输入排序值" />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-switch
            v-model="form.isActive"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" class="glass-btn-cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" class="glass-btn-confirm">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import StatCard from '@/components/dashboard/StatCard.vue';
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper';

import { 
  CategoryStats 
} from '@/utils/categoryManager';
import { 
  CATEGORY_STATUS, 
  CATEGORY_STATUS_LABELS, 
  CATEGORY_STATUS_COLORS,
  PAGINATION_CONFIG
} from '@/utils/categoryConstants';

export default {
  name: 'CategoryManagePage',
  middleware: 'admin-only',
  components: {
    PageHeader,
    EmptyState,
    StatCard
  },
  
  data() {
    return {
      categoryList: [],
      allCategoryList: [], // 全部分类数据，用于统计卡片
      total: 0,
      searchQuery: '',
      filterStatus: 'all',
      currentPage: PAGINATION_CONFIG.DEFAULT_PAGE,
      pageSize: 20,
      selectedIds: [],
      
      // Dialog related
      dialogVisible: false,
      dialogTitle: '新增分类',
      submitLoading: false,
      form: {
        categoryId: undefined,
        categoryName: '',
        categoryCode: '',
        sortOrder: 0,
        isActive: 1
      },
      rules: {
        categoryName: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        categoryCode: [
          { required: true, message: '请输入分类代码', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和连字符', trigger: 'blur' }
        ],
        sortOrder: [
          { required: true, message: '请输入排序值', trigger: 'blur' }
        ]
      },

      PAGINATION_CONFIG
    };
  },

  computed: {
    statCardsData() {
      const cards = CategoryStats.generateStatCards(this.allCategoryList);
      return cards;
    }
  },

  created() {
    this.fetchAllCategories();
    this.fetchCategories();
  },

  watch: {
    searchQuery(newVal) {
      if (!newVal) {
        this.handleSearch();
      }
    }
  },

  methods: {
    // 获取全部分类数据（用于统计卡片）
    async fetchAllCategories() {
      try {
        const response = await this.$axios.get('/api/categories');
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          this.allCategoryList = response.data || [];
        }
      } catch (error) {
        console.error('Fetch all categories error:', error);
      }
    },

    // 获取分类列表（支持筛选）
    async fetchCategories() {
      try {
        const params = {
          page: this.currentPage,
          current: this.currentPage,
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          size: this.pageSize,
          keyword: this.searchQuery
        };

        if (this.filterStatus !== 'all') {
          params.isActive = this.filterStatus;
        }

        const response = await this.$axios.get('/api/categories/page', { params });

        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          const data = response.data || {};
          this.categoryList = data.records || data.list || [];
          this.total = data.total || 0;
        } else {
           MessageHelper.error(this, response.message || '获取分类列表失败');
        }
      } catch (error) {
        console.error('Fetch categories error:', error);
        MessageHelper.error(this, '获取分类列表失败');
      }
    },

    getStatusLabel(status) {
      return CATEGORY_STATUS_LABELS[status] || '未知';
    },

    getStatusColor(status) {
      return CATEGORY_STATUS_COLORS[status] || 'info';
    },

    handleStatClick(status) {
      this.filterStatus = status;
      this.currentPage = 1;
      this.fetchCategories();
    },

    handleSearch() {
      this.currentPage = 1;
      this.fetchCategories();
    },

    handlePageChange(page) {
      this.currentPage = page;
      this.fetchCategories();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
      this.fetchCategories();
    },

    handleClearFilters() {
      this.searchQuery = '';
      this.filterStatus = 'all';
      this.currentPage = 1;
      this.fetchCategories();
    },

    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.categoryId);
    },

    // Dialog & CRUD
    handleAdd() {
      this.dialogTitle = '新增分类';
      this.form = {
        categoryId: undefined,
        categoryName: '',
        categoryCode: '',
        sortOrder: 0,
        isActive: 1
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },

    handleEdit(row) {
      this.dialogTitle = '编辑分类';
      this.form = { ...row }; // Copy row data
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.form.clearValidate();
      });
    },

    submitForm() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          this.submitLoading = true;
          try {
            let response;
            if (this.form.categoryId) {
              // Update
              response = await this.$axios.put(`/api/categories/${this.form.categoryId}`, this.form);
            } else {
              // Create
              response = await this.$axios.post('/api/categories', this.form);
            }

            if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
              MessageHelper.success(this, this.form.categoryId ? '更新成功' : '新增成功');
              this.dialogVisible = false;
              this.fetchAllCategories();
              this.fetchCategories();
            } else {
              MessageHelper.error(this, response.message || '操作失败');
            }
          } catch (error) {
            console.error('Submit category error:', error);
            MessageHelper.error(this, '操作异常');
          } finally {
            this.submitLoading = false;
          }
        }
      });
    },

    async handleToggleStatus(row) {
      try {
        const action = row.isActive === 1 ? 'deactivate' : 'activate';
        const actionText = row.isActive === 1 ? '禁用' : '启用';
        
        await ConfirmHelper.confirm(
          this, 
          `确认${actionText}该分类吗？`, 
          `分类: ${row.categoryName}`
        );
        
        const response = await this.$axios.post(`/api/categories/${row.categoryId}/${action}`);
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, `${actionText}成功`);
          this.fetchAllCategories();
          this.fetchCategories();
        } else {
          MessageHelper.error(this, response.message || `${actionText}失败`);
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Toggle status error:', error);
          MessageHelper.error(this, '操作异常');
        }
      }
    },

    async handleDelete(row) {
      try {
        await ConfirmHelper.confirmDelete(this, `分类: ${row.categoryName}`);
        
        const response = await this.$axios.delete(`/api/categories/${row.categoryId}`);
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '删除成功');
          if (this.categoryList.length === 1 && this.currentPage > 1) {
            this.currentPage--;
          }
          this.fetchAllCategories();
          this.fetchCategories();
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
        await ConfirmHelper.confirmDelete(this, `选中的 ${this.selectedIds.length} 个分类`);
        
        const response = await this.$axios.delete('/api/categories/batch', {
          data: this.selectedIds
        });
        
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          MessageHelper.success(this, '批量删除成功');
          this.selectedIds = [];
          this.fetchAllCategories();
          this.fetchCategories();
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

.category-manage-page {
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

  .btn-primary {
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
      rgba(var(--color-primary-rgb), 0.95), 
      rgba(var(--color-primary-rgb), 0.85));
    border: 1px solid rgba(var(--color-primary-rgb), 0.3);
    color: #fff;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;

    i {
      font-size: 16px;
      font-weight: bold;
    }

    &:hover {
      background: linear-gradient(135deg, 
        rgba(var(--color-primary-rgb), 1), 
        rgba(var(--color-primary-rgb), 0.9));
      border-color: rgba(var(--color-primary-rgb), 0.5);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
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
  cursor: pointer; // 添加鼠标指针样式
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

.category-table-wrapper {
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

.text-primary {
  color: $color-primary;
  &:hover { opacity: 0.8; }
}

.text-success {
  color: #67c23a;
  &:hover { color: #85ce61; }
}

.text-warning {
  color: #e6a23c;
  &:hover { color: #ebb563; }
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

  // Dialog styles
  .glass-dialog {
    background: var(--color-bg-surface) !important;
    border-radius: 20px !important;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2) !important;
    border: 1px solid var(--color-border);
    @include glass-effect;

    .el-dialog__header {
      border-bottom: 1px solid var(--color-border);
      padding: 20px 24px;
      .el-dialog__title {
        color: $color-text-primary;
        font-weight: 600;
      }
    }

    .el-dialog__body {
      padding: 24px;
      color: $color-text-primary;
    }

    .el-dialog__footer {
      border-top: 1px solid var(--color-border);
      padding: 16px 24px;
    }
    
    .el-form-item__label {
      color: $color-text-primary;
    }
    
    .el-input__inner {
      background: rgba(255, 255, 255, 0.05);
      border-color: var(--color-border);
      color: $color-text-primary;
    }

    .el-input-number {
      width: 100%;
      .el-input-number__decrease, .el-input-number__increase {
        background: rgba(255, 255, 255, 0.05);
        border-color: var(--color-border);
        color: $color-text-primary;
      }
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
