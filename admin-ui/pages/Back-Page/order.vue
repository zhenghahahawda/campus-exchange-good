<template>
  <div class="order-page">
    <!-- 页面头部 -->
    <div class="page-header-wrapper">
      <PageHeader 
        title="订单管理" 
        description="管理平台所有交易订单，查看交换进度"
      />
    </div>

    <!-- 统计卡片 -->
    <div class="section-container">
      <StatCards
        :filterStatus.sync="filterStatus"
        :cards="statCards"
      />
    </div>

    <!-- 筛选工具栏 -->
    <OrderFilterBar 
      :searchQuery.sync="searchQuery"
      :sortBy.sync="sortBy"
    />

    <!-- 订单列表 -->
    <OrderList 
      v-if="paginatedOrders.length > 0"
      :orders="paginatedOrders"
      @view-detail="handleViewDetail"
      @cancel="handleCancel"
      @delete="orderManager.handleDelete"
      @move-to-process="handleMoveToProcess"
      @handle-order="handleMarkCompleted"
      @complete-processing="handleCompleteProcessing"
      @mark-cancelled="handleMarkCancelled"
    />

    <!-- 分页 -->
    <div v-if="filteredOrders.length > 0" class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[4, 10, 20, 50]"
        :page-size="pageSize"
        :total="filteredOrders.length"
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
      icon="el-icon-s-order"
      message="暂无符合条件的订单"
      action-text="清除筛选条件"
      :show-action="true"
      @action="handleClearFilters"
    />

    <!-- 弹窗 -->
    <OrderDialogs
      :detailVisible.sync="detailDialogVisible"
      :cancelVisible.sync="cancelDialogVisible"
      :markCompletedVisible.sync="markCompletedDialogVisible"
      :markCancelledVisible.sync="markCancelledDialogVisible"
      :currentOrder="currentOrder"
      :cancelReason.sync="cancelReason"
      :adminNote.sync="adminNote"
      @confirm-cancel="handleConfirmCancel"
      @update-remark="handleUpdateRemark"
      @update-admin-note="handleUpdateAdminNote"
      @confirm-mark-completed="handleConfirmMarkCompleted"
      @confirm-mark-cancelled="handleConfirmMarkCancelled"
    />
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue';
import StatCards from '@/components/common/StatCards.vue';
import OrderFilterBar from '@/components/order/OrderFilterBar.vue';
import OrderList from '@/components/order/OrderList.vue';
import OrderDialogs from '@/components/order/OrderDialogs.vue';
import EmptyState from '@/components/ui/EmptyState.vue';
import { useOrderManagement } from '@/composables/useOrders';
import { ORDER_STATUS, ORDER_STATUS_LABELS, ORDER_SORT_OPTIONS } from '@/utils/orderConstants';
import { isSuccess } from '@/composables/useApi';

/**
 * 订单管理页面 V3.0
 * @description 管理平台所有交易订单，支持筛选、排序、分页和状态流转
 * @author 系统管理员
 * @date 2026-03-07
 * 
 * 订单状态流转：
 * 1. 待交易 (pending) → 已取消 (cancelled)
 * 2. 已完成 (completed) → 待处理 (to_process)
 * 3. 待处理 (to_process) → 已完成 (completed) 或 已取消 (cancelled)
 * 4. 已取消 (cancelled) → 删除
 */
export default {
  name: 'OrderManagementPage',
  middleware: 'admin-only', // 仅管理员可访问

  components: {
    PageHeader,
    StatCards,
    OrderFilterBar,
    OrderList,
    OrderDialogs,
    EmptyState
  },
  
  data() {
    return {
      searchQuery: '',
      filterStatus: ORDER_STATUS.ALL,
      sortBy: ORDER_SORT_OPTIONS.DEFAULT,
      detailDialogVisible: false,
      cancelDialogVisible: false,
      markCompletedDialogVisible: false,
      markCancelledDialogVisible: false,
      currentOrder: null,
      currentCancelOrder: null,
      currentProcessOrder: null,
      cancelReason: '',
      adminNote: '',
      // 分页相关
      currentPage: 1,
      pageSize: 4,
      orderList: [], // 从API获取数据
      orderManager: null
    };
  },
  
  computed: {
    statCards() {
      const s = this.orderManager
        ? this.orderManager.getStats()
        : { pending: 0, completed: 0, toProcess: 0, processing: 0, cancelled: 0 };
      const total = this.orderList ? this.orderList.length : 0;
      return [
        { status: ORDER_STATUS.ALL, label: '订单总数', count: total, icon: 'el-icon-s-order', iconBg: 'bg-primary-soft', valueClass: '' },
        { status: ORDER_STATUS.PENDING, label: ORDER_STATUS_LABELS[ORDER_STATUS.PENDING], count: s.pending, icon: 'el-icon-time', iconBg: 'bg-warning-soft', valueClass: 'text-warning' },
        { status: ORDER_STATUS.COMPLETED, label: ORDER_STATUS_LABELS[ORDER_STATUS.COMPLETED], count: s.completed, icon: 'el-icon-circle-check', iconBg: 'bg-success-soft', valueClass: 'text-success' },
        { status: ORDER_STATUS.TO_PROCESS, label: ORDER_STATUS_LABELS[ORDER_STATUS.TO_PROCESS], count: s.toProcess, icon: 'el-icon-document', iconBg: 'bg-info-soft', valueClass: 'text-info' },
        { status: ORDER_STATUS.PROCESSING, label: ORDER_STATUS_LABELS[ORDER_STATUS.PROCESSING], count: s.processing, icon: 'el-icon-loading', iconBg: 'bg-primary-soft', valueClass: 'text-primary' },
        { status: ORDER_STATUS.CANCELLED, label: ORDER_STATUS_LABELS[ORDER_STATUS.CANCELLED], count: s.cancelled, icon: 'el-icon-circle-close', iconBg: 'bg-danger-soft', valueClass: 'text-danger' }
      ];
    },
    
    filteredOrders() {
      if (!this.orderManager || !this.orderList) {
        return [];
      }
      return this.orderManager.getFilteredOrders(
        this.filterStatus, 
        this.searchQuery, 
        this.sortBy
      );
    },
    
    // 分页后的订单列表
    paginatedOrders() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredOrders.slice(start, end);
    },
    
    // 总页数
    totalPages() {
      return Math.ceil(this.filteredOrders.length / this.pageSize);
    }
  },
  
  created() {
    // 初始化订单管理器
    this.orderManager = useOrderManagement(this, []);
    // 从API获取订单数据
    this.fetchOrders();
  },

  methods: {
    /**
     * 获取订单列表
     */
    async fetchOrders() {
      try {
        // Request all orders with a large page size to get all records
        const response = await this.$axios.get('/api/admin/orders', {
          params: {
            pageNum: 1,
            pageSize: 1000 // Large enough to get all orders
          }
        });

        // console.log('Orders API Response:', response);

        // axios拦截器已经解包了response，直接使用response
        let pageData = response;

        // 如果response有data属性，可能是标准的Result结构
        if (response.data && response.code) {
          pageData = response.data;
        }

        // 提取records数组
        const rawList = pageData.records || [];

        // console.log('Raw order list:', rawList);

        if (!this.orderManager) {
          this.orderManager = useOrderManagement(this, []);
        }

        this.orderList = rawList.map(item => this.orderManager.normalizeOrder(item));
        this.orderManager = useOrderManagement(this, this.orderList);

        // console.log('Normalized order list:', this.orderList);
      } catch (error) {
        console.error('获取订单数据异常:', error);
        this.$root.$emit('show-island-message', '获取订单数据失败', 'danger');
      }
    },

    /**
     * 查看订单详情
     * @param {Object} order - 订单对象
     */
    handleViewDetail(order) {
      this.orderManager.handleViewDetail(order, async (orderData) => {
        this.currentOrder = orderData;
        this.detailDialogVisible = true;
        // 获取凭证数据
        await this.fetchOrderProofs(orderData);
      });
    },

    /**
     * 获取订单凭证数据
     */
    async fetchOrderProofs(order) {
      try {
        const res = await this.$axios.get(`/api/admin/orders/${order.id}/proofs`);
        const proofs = isSuccess(res) ? (res.data || []) : (Array.isArray(res) ? res : []);

        const processImages = (imgStr) => {
          if (!imgStr) return [];
          if (Array.isArray(imgStr)) return imgStr;
          try {
            if (typeof imgStr === 'string' && imgStr.startsWith('[')) return JSON.parse(imgStr);
            return imgStr.split(',');
          } catch (e) { return []; }
        };

        const exchangeProofs = {
          buyer: { uploaded: false, images: [], comment: '' },
          seller: { uploaded: false, images: [], comment: '' }
        };

        proofs.forEach(p => {
          const side = p.userRole === 'initiator' ? 'buyer' : 'seller';
          exchangeProofs[side] = {
            uploaded: p.status >= 1,
            images: processImages(p.images),
            comment: p.comment || ''
          };
        });

        this.$set(this.currentOrder, 'exchangeProofs', exchangeProofs);
      } catch (e) {
        console.error('获取凭证数据失败:', e);
      }
    },
    
    /**
     * 更新订单备注
     * @param {Object} payload - 包含 orderId 和 remark 的对象
     */
    async handleUpdateRemark({ orderId, remark }) {
      try {
        const res = await this.$axios.put(`/api/admin/orders/${orderId}/remark`, { remark });
        if (isSuccess(res)) {
           const order = this.orderList.find(o => o.id === orderId);
           if (order) {
             order.remark = remark;
           }
        } else {
           this.$root.$emit('show-island-message', res.message || '更新备注失败', 'danger');
        }
      } catch(e) {
         console.error(e);
      }
    },

    /**
     * 更新管理员备注
     * @param {Object} payload - 包含 orderId 和 adminNote 的对象
     */
    async handleUpdateAdminNote({ orderId, adminNote }) {
      try {
        const res = await this.$axios.put(`/api/admin/orders/${orderId}/status`, {
          status: this.currentOrder.status,
          adminNote: adminNote
        });
        if (isSuccess(res)) {
           const order = this.orderList.find(o => o.id === orderId);
           if (order) {
             order.adminNote = adminNote;
           }
           // 更新当前订单对象
           if (this.currentOrder && this.currentOrder.id === orderId) {
             this.currentOrder.adminNote = adminNote;
           }
        } else {
           this.$root.$emit('show-island-message', res.message || '更新管理员备注失败', 'danger');
        }
      } catch(e) {
         console.error(e);
         this.$root.$emit('show-island-message', '更新管理员备注失败', 'danger');
      }
    },
    
    /**
     * 打开取消订单弹窗
     * @param {Object} order - 要取消的订单
     */
    handleCancel(order) {
      this.currentCancelOrder = order;
      this.cancelReason = '';
      this.cancelDialogVisible = true;
    },
    
    /**
     * 确认取消订单
     */
    async handleConfirmCancel() {
      if (await this.orderManager.handleCancelFromPending(this.currentCancelOrder, this.cancelReason)) {
        this.cancelDialogVisible = false;
      }
    },
    
    /**
     * 将已完成订单转为待处理
     * @param {Object} order - 订单对象
     */
    handleMoveToProcess(order) {
      this.$confirm('确认将此订单转为待处理状态？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        if (await this.orderManager.handleMoveToProcess(order)) {
          // 成功
        }
      }).catch(() => {});
    },
    
    /**
     * 打开标记完成弹窗
     * @param {Object} order - 订单对象
     */
    handleMarkCompleted(order) {
      this.currentProcessOrder = order;
      this.adminNote = '';
      this.markCompletedDialogVisible = true;
    },
    
    /**
     * 确认标记订单为完成
     */
    async handleConfirmMarkCompleted() {
      if (!this.adminNote || this.adminNote.trim() === '') {
        this.$root.$emit('show-island-message', '请填写处理方案', 'warning');
        return;
      }
      if (await this.orderManager.handleStartProcessing(this.currentProcessOrder, this.adminNote)) {
        this.markCompletedDialogVisible = false;
      }
    },
    
    /**
     * 完成处理中的订单
     * @param {Object} order - 订单对象
     */
    handleCompleteProcessing(order) {
      this.$confirm('确认该订单已处理完成？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        await this.orderManager.handleCompleteProcessing(order);
      }).catch(() => {});
    },

    /**
     * 打开标记取消弹窗
     * @param {Object} order - 订单对象
     */
    handleMarkCancelled(order) {
      this.currentProcessOrder = order;
      this.adminNote = '';
      this.markCancelledDialogVisible = true;
    },
    
    /**
     * 确认标记订单为取消
     */
    async handleConfirmMarkCancelled() {
      if (!this.adminNote || this.adminNote.trim() === '') {
        this.$root.$emit('show-island-message', '请填写处理备注', 'warning');
        return;
      }
      if (await this.orderManager.handleMarkAsCancelled(this.currentProcessOrder, this.adminNote)) {
        this.markCancelledDialogVisible = false;
      }
    },
    
    /**
     * 清除所有筛选条件
     */
    handleClearFilters() {
      this.filterStatus = ORDER_STATUS.ALL;
      this.searchQuery = '';
      this.sortBy = ORDER_SORT_OPTIONS.DEFAULT;
      this.currentPage = 1;
    },
    
    /**
     * 分页切换处理
     * @param {number} page - 目标页码
     */
    handlePageChange(page) {
      this.currentPage = page;
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    /**
     * 处理每页显示数量变化
     * @param {number} size - 每页显示数量
     */
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

.order-page {
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
