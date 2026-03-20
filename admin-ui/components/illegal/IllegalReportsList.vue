<template>
  <div class="reports-container">
    <div v-if="reports.length === 0" class="empty-wrapper">
      <EmptyState 
        icon="el-icon-warning-outline"
        message="暂无举报数据"
        :show-action="true"
        action-text="刷新数据"
        @action="$emit('refresh')"
      />
    </div>
    
    <div v-else class="reports-grid">
      <IllegalReportCard
        v-for="report in reports" 
        :key="report.id"
        :report="report"
        @view-detail="$emit('view-detail', $event)"
        @show-action-dialog="$emit('show-action-dialog', $event)"
        @show-progress="$emit('show-progress', $event)"
        @complete-processing="$emit('complete-processing', $event)"
        @show-result="$emit('show-result', $event)"
        @show-reject-dialog="$emit('show-reject-dialog', $event)"
        @set-processing="$emit('set-processing', $event)"
      />
    </div>
  </div>
</template>

<script>
import EmptyState from '@/components/ui/EmptyState.vue'
import IllegalReportCard from './IllegalReportCard.vue'

export default {
  name: 'IllegalReportsList',
  
  components: {
    EmptyState,
    IllegalReportCard
  },
  
  props: {
    reports: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style lang="scss" scoped>
.reports-container {
  margin-bottom: 32px;
}

.empty-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  align-items: stretch;
}
</style>