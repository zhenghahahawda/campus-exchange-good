<template>
  <div class="violations-page">
    <div class="page-header">
      <div class="header-text">
        <h1>违规举报</h1>
        <p>查看我提交的举报记录</p>
      </div>
    </div>

    <!-- 列表 -->
    <div class="list-card" v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <i class="el-icon-warning-outline"></i>
        <p>暂无举报记录</p>
      </div>

      <div v-for="item in list" :key="item.id" class="report-item" @click="openDetail(item)">
        <div class="item-left">
          <span class="target-badge" :class="item.targetType">
            {{ item.targetType === 'product' ? '商品' : '用户' }}
          </span>
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-desc">{{ item.description }}</div>
            <div class="item-meta">
              <span>{{ formatType(item.reportType) }}</span>
              <span class="dot">·</span>
              <span>{{ formatTime(item.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="item-right">
          <span class="status-tag" :class="item.status">{{ STATUS_TEXT[item.status] || item.status }}</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page.sync="page"
        @current-change="loadList"
      />
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      :visible.sync="showDetail"
      title="举报详情"
      width="480px"
      :lock-scroll="false"
      append-to-body
      custom-class="report-detail-dialog"
    >
      <template v-if="detail">
        <div class="detail-row"><span class="label">举报编号</span><span>{{ detail.reportNumber }}</span></div>
        <div class="detail-row"><span class="label">举报对象</span><span>{{ detail.targetType === 'product' ? '商品' : '用户' }} #{{ detail.targetId }}</span></div>
        <div class="detail-row"><span class="label">举报类型</span><span>{{ formatType(detail.reportType) }}</span></div>
        <div class="detail-row"><span class="label">标题</span><span>{{ detail.title }}</span></div>
        <div class="detail-row"><span class="label">描述</span><span>{{ detail.description }}</span></div>
        <div class="detail-row" v-if="detail.detailContent"><span class="label">详细说明</span><span>{{ detail.detailContent }}</span></div>
        <div class="detail-row"><span class="label">状态</span>
          <span class="status-tag" :class="detail.status">{{ STATUS_TEXT[detail.status] || detail.status }}</span>
        </div>
        <div class="detail-row" v-if="detail.handleResult"><span class="label">处理结果</span><span>{{ detail.handleResult }}</span></div>
        <div class="detail-row" v-if="detail.rejectReason"><span class="label">驳回原因</span><span>{{ detail.rejectReason }}</span></div>
        <div class="detail-row"><span class="label">提交时间</span><span>{{ formatTime(detail.createdAt) }}</span></div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
const STATUS_TEXT = {
  pending: '待处理',
  processing: '处理中',
  approved: '已通过',
  rejected: '已驳回'
}

const REPORT_TYPE_TEXT = {
  'product-fake': '虚假商品信息',
  'product-prohibited': '违禁物品',
  'product-copyright': '侵权商品',
  'product-price': '价格欺诈',
  'product-quality': '质量问题',
  'product-description': '描述不符',
  'user-harassment': '骚扰行为',
  'user-fraud': '欺诈行为',
  'user-spam': '垃圾信息',
  'user-impersonation': '冒充他人',
  'user-inappropriate': '不当内容',
  'user-transaction': '交易纠纷'
}

export default {
  name: 'ViolationsPage',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      page: 1,
      pageSize: 20,
      showDetail: false,
      detail: null,
      STATUS_TEXT
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const res = await this.$axios.get('/violations/my', {
          params: { page: this.page, limit: this.pageSize }
        })
        if (res.code === 20000 && res.data) {
          this.list = res.data.list || res.data || []
          this.total = res.data.total || this.list.length
        }
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    async openDetail(item) {
      try {
        const res = await this.$axios.get(`/violations/${item.id}`)
        if (res.code === 20000) {
          this.detail = res.data
          this.showDetail = true
        }
      } catch (e) {
        // fallback to list data
        this.detail = item
        this.showDetail = true
      }
    },
    formatType(type) {
      return REPORT_TYPE_TEXT[type] || type
    },
    formatTime(str) {
      if (!str) return ''
      const d = new Date(str)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
  },
  head() {
    return { title: '我的举报 - 校园换物平台' }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.violations-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  min-height: 100vh;
  color: $color-text-primary;
}

.page-header {
  margin-bottom: 20px;
  h1 { font-size: 28px; font-weight: $font-weight-bold; margin-bottom: 6px; }
  p { font-size: 14px; color: $color-text-secondary; margin: 0; }
}

.list-card {
  @include glass-card;
  padding: 0;
  overflow: hidden;
  min-height: 120px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
  color: $color-text-disabled;
  i { font-size: 48px; margin-bottom: 12px; display: block; opacity: 0.4; }
  p { margin: 0; font-size: 14px; }
}

.report-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;

  &:last-child { border-bottom: none; }
  &:hover { background: rgba(var(--color-primary-rgb), 0.05); }

  .item-left {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    flex: 1;
    min-width: 0;
  }

  .target-badge {
    flex-shrink: 0;
    padding: 2px 8px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    margin-top: 2px;
    &.product { background: rgba(64,158,255,0.15); color: #409EFF; }
    &.user { background: rgba(230,162,60,0.15); color: #E6A23C; }
  }

  .item-info {
    flex: 1;
    min-width: 0;
    .item-title { font-size: 14px; font-weight: 600; color: $color-text-primary; margin-bottom: 4px; }
    .item-desc { font-size: 13px; color: $color-text-secondary; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
    .item-meta { font-size: 12px; color: $color-text-disabled; display: flex; gap: 4px; align-items: center; }
    .dot { opacity: 0.5; }
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
    .el-icon-arrow-right { color: $color-text-disabled; font-size: 14px; }
  }
}

.status-tag {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  &.pending { background: rgba(230,162,60,0.15); color: #E6A23C; }
  &.processing { background: rgba(64,158,255,0.15); color: #409EFF; }
  &.approved { background: rgba(103,194,58,0.15); color: #67C23A; }
  &.rejected { background: rgba(245,108,108,0.15); color: #F56C6C; }
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  font-size: 14px;
  &:last-child { border-bottom: none; }
  .label { color: $color-text-secondary; flex-shrink: 0; width: 72px; }
}
</style>
