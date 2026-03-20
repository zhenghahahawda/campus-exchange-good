<template>
  <div class="goods-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader 
        title="二手商品管理" 
        description="审核与管理用户发布的二手闲置商品"
      />
      <button class="glass-btn btn-primary" @click="handleAddProduct">
        <i class="el-icon-plus"></i>
        <span>发布商品</span>
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="section-container">
      <StatCards
        :filterStatus.sync="filterStatus"
        :cards="statCards"
      />
    </div>

    <!-- 筛选工具栏 -->
    <FilterBar
      :searchQuery.sync="searchQuery"
      :sortBy.sync="sortBy"
      :filterCategory.sync="filterCategory"
      @add-product="handleAddProduct"
    />

    <!-- 商品列表 -->
    <GoodsList 
      v-if="paginatedGoods.length > 0"
      :goods="paginatedGoods"
      @preview="goodsManager.handlePreview"
      @edit="handleEdit"
      @delete="goodsManager.handleDelete"
      @approve="goodsManager.handleApprove"
      @reject="handleReject"
      @toggle-shelf="goodsManager.handleToggleShelf"
    />

    <!-- 分页 -->
    <div v-if="filteredGoods.length > 0" class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[8, 16, 24, 32]"
        :page-size="pageSize"
        :total="filteredGoods.length"
        :pager-count="5"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="glass-pagination"
      />
    </div>

    <!-- 空状态 -->
    <EmptyState
      v-else
      icon="el-icon-box"
      message="暂无符合条件的商品"
      action-text="清除筛选条件"
      :show-action="true"
      @action="handleClearFilters"
    />

    <!-- 弹窗 -->
    <GoodDialogs 
      :rejectVisible.sync="rejectDialogVisible"
      :rejectReason.sync="rejectReason"
      :formVisible.sync="dialogVisible"
      :isEditMode="isEditMode"
      :currentForm="currentForm"
      @confirm-reject="handleConfirmReject"
      @save-product="handleSaveProduct"
      @edit-product="handleEditProduct"
    />
    
    <!-- 交换凭证码弹窗 -->
    <ExchangeCodeDialog
      :visible.sync="codeDialogVisible"
      :exchangeCode="currentExchangeCode"
    />
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue';
import StatCards from '@/components/common/StatCards.vue';
import FilterBar from '@/components/goods/FilterBar.vue';
import GoodsList from '@/components/goods/GoodsList.vue';
import GoodDialogs from '@/components/goods/GoodDialogs.vue';
import ExchangeCodeDialog from '@/components/goods/ExchangeCodeDialog.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import { useGoodsManagement, useGoodsDialogs } from '@/composables/useGoods';
import { GOOD_STATUS, SORT_OPTIONS } from '@/utils/constants';

export default {
  name: 'SecondHandGoodsPage',
  middleware: 'admin-only', // 仅管理员可访问
  components: {
    PageHeader,
    StatCards,
    FilterBar,
    GoodsList,
    GoodDialogs,
    ExchangeCodeDialog,
    EmptyState
  },
  data() {
    return {
      searchQuery: '',
      filterStatus: GOOD_STATUS.ALL,
      filterCategory: '',
      sortBy: SORT_OPTIONS.DEFAULT,
      dialogVisible: false,
      rejectDialogVisible: false,
      codeDialogVisible: false,
      rejectReason: '',
      currentRejectGood: null,
      currentExchangeCode: '',
      isEditMode: false,
      currentForm: useGoodsDialogs().getInitialForm(),
      goodsList: [], // 从API获取数据
      // 分页相关
      currentPage: 1,
      pageSize: 8
    };
  },
  computed: {
    statCards() {
      const s = this.goodsManager.getStats();
      return [
        { status: GOOD_STATUS.ALL, label: '商品总数', count: s.total, icon: 'el-icon-goods', iconBg: 'bg-primary-soft', valueClass: '' },
        { status: GOOD_STATUS.PENDING, label: '待审核', count: s.pending, icon: 'el-icon-time', iconBg: 'bg-warning-soft', valueClass: 'text-warning' },
        { status: GOOD_STATUS.ACTIVE, label: '出售中', count: s.active, icon: 'el-icon-shopping-cart-2', iconBg: 'bg-success-soft', valueClass: 'text-success' },
        { status: GOOD_STATUS.SOLD, label: '已售出', count: s.sold, icon: 'el-icon-sold-out', iconBg: 'bg-info-soft', valueClass: 'text-info' },
        { status: GOOD_STATUS.OFF_SHELF, label: '已下架', count: s.offShelf, icon: 'el-icon-download', iconBg: 'bg-secondary-soft', valueClass: 'text-secondary' },
        { status: GOOD_STATUS.REJECTED, label: '已驳回', count: s.rejected, icon: 'el-icon-close-notification', iconBg: 'bg-danger-soft', valueClass: 'text-danger' }
      ];
    },
    
    filteredGoods() {
      let result = this.goodsManager.getFilteredGoods(
        this.filterStatus,
        this.searchQuery,
        this.sortBy
      );
      if (this.filterCategory) {
        result = result.filter(g => String(g.categoryId) === this.filterCategory);
      }
      return result;
    },
    
    // 分页后的商品列表
    paginatedGoods() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredGoods.slice(start, end);
    },
    
    // 总页数
    totalPages() {
      return Math.ceil(this.filteredGoods.length / this.pageSize);
    }
  },
  created() {
    // 初始化商品管理器
    this.goodsManager = useGoodsManagement(this, []);
    // 从API获取商品数据
    this.fetchGoods();
  },
  methods: {
    // 获取商品列表
    async fetchGoods() {
      try {
        const response = await this.$axios.get('/api/goods');
        // 修正：拦截器已经解包了 response.data，所以这里 response 就是后端返回的 json
        // 兼容多种成功状态码：20000、200、0
        if (response && (response.code === 20000 || response.code === 200 || response.code === 0)) {
          // 对返回的数据进行规范化处理
          const rawList = response.data || [];
          
          // 这里的 this.goodsManager 是组件创建时初始化的实例，包含 normalizeGood 方法
          this.goodsList = rawList.map(item => this.goodsManager.normalizeGood(item));
          
          // 更新管理器的列表状态
          this.goodsManager = useGoodsManagement(this, this.goodsList);
        } else {
          console.error('获取商品数据失败:', response.message);
          this.$root.$emit('show-island-message', response.message || '获取商品数据失败', 'danger');
        }
      } catch (error) {
        console.error('获取商品数据失败:', error);
        // 拦截器已处理
      }
    },

    handleEdit(good) {
      this.goodsManager.handleEdit(good, (form) => {
        this.isEditMode = true;
        this.currentForm = form;
        this.dialogVisible = true;
      });
    },
    
    handleEditProduct() {
      // 当用户在查看详情时点击"编辑商品"按钮，关闭当前对话框并重新打开编辑模式
      this.dialogVisible = false;
      this.$nextTick(() => {
        this.isEditMode = false;
        this.dialogVisible = true;
      });
    },

    handleAddProduct() {
      this.isEditMode = false;
      this.currentForm = useGoodsDialogs().getInitialForm();
      this.dialogVisible = true;
    },
    
    handleReject(good) {
      this.currentRejectGood = good;
      this.rejectReason = '';
      this.rejectDialogVisible = true;
    },
    
    handleConfirmReject() {
      if (this.goodsManager.handleReject(this.currentRejectGood, this.rejectReason)) {
        this.rejectDialogVisible = false;
      }
    },
    
    handleSaveProduct() {
      if (this.goodsManager.handleSave(this.currentForm)) {
        this.dialogVisible = false;
      }
    },
    
    handleClearFilters() {
      this.goodsManager.clearFilters((filters) => {
        Object.assign(this, filters);
      });
      this.filterCategory = '';
      this.currentPage = 1;
    },
    
    // 分页相关方法
    handlePageChange(page) {
      this.currentPage = page;
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    handleSizeChange(size) {
      this.pageSize = size;
      this.currentPage = 1;
    }
  },
  
  watch: {
    // 当筛选条件改变时，重置到第一页
    filterStatus() {
      this.currentPage = 1;
    },
    filterCategory() {
      this.currentPage = 1;
    },
    searchQuery() {
      this.currentPage = 1;
    },
    sortBy() {
      this.currentPage = 1;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.goods-page {
  padding: 10px 20px;
  min-height: 100vh;
  color: $color-text-primary;
}

// 页面头部包装器
.page-header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
  
  .glass-btn.btn-primary {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
    backdrop-filter: blur(10px);
    background: linear-gradient(135deg, 
      rgba(var(--color-primary-rgb), 0.95), 
      rgba(var(--color-primary-rgb), 0.85));
    border: 1px solid rgba(var(--color-primary-rgb), 0.3);
    color: #fff;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    
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
}

.section-container {
  margin-bottom: 32px;
}

// 分页样式
@import '@/assets/css/components/pagination.scss';
</style>

<style lang="scss">
@import '@/assets/css/global-message-box.scss';
</style>
