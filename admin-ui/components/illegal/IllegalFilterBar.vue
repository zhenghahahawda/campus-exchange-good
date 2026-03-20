<template>
  <div class="filter-bar">
    <div class="search-wrapper">
      <el-input
        :value="search"
        @input="$emit('update:search', $event)"
        placeholder="搜索举报内容/举报人/被举报人..."
        prefix-icon="el-icon-search"
        clearable
        class="capsule-input"
      />
    </div>
    
    <div class="filter-controls">
      <GlassDropdown @command="$emit('target-change', $event)">
        <template slot="trigger" slot-scope="{ isOpen }">
          <div class="filter-dropdown">
            <span>{{ getTargetLabel(target) }}</span>
            <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
          </div>
        </template>
        <GlassDropdownItem 
          v-for="(label, key) in REPORT_TARGET_LABELS" 
          :key="key"
          :command="key"
        >
          {{ label }}
        </GlassDropdownItem>
      </GlassDropdown>

      <GlassDropdown @command="$emit('type-change', $event)">
        <template slot="trigger" slot-scope="{ isOpen }">
          <div class="filter-dropdown">
            <span>{{ getTypeLabel(type) }}</span>
            <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
          </div>
        </template>
        <GlassDropdownItem 
          v-for="(label, key) in REPORT_TYPE_LABELS" 
          :key="key"
          :command="key"
        >
          {{ label }}
        </GlassDropdownItem>
      </GlassDropdown>

      <GlassDropdown @command="$emit('status-change', $event)">
        <template slot="trigger" slot-scope="{ isOpen }">
          <div class="filter-dropdown">
            <span>{{ getStatusLabel(status) }}</span>
            <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
          </div>
        </template>
        <GlassDropdownItem 
          v-for="(label, key) in REPORT_STATUS_LABELS" 
          :key="key"
          :command="key"
        >
          {{ label }}
        </GlassDropdownItem>
      </GlassDropdown>
    </div>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue'
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue'
import {
  REPORT_TARGET_LABELS,
  REPORT_TYPE_LABELS,
  REPORT_STATUS_LABELS
} from '@/utils/illegalConstants'

export default {
  name: 'IllegalFilterBar',
  
  components: {
    GlassDropdown,
    GlassDropdownItem
  },
  
  props: {
    search: {
      type: String,
      default: ''
    },
    target: {
      type: String,
      default: 'all'
    },
    type: {
      type: String,
      default: 'all'
    },
    status: {
      type: String,
      default: 'all'
    }
  },
  
  data() {
    return {
      REPORT_TARGET_LABELS,
      REPORT_TYPE_LABELS,
      REPORT_STATUS_LABELS
    }
  },
  
  methods: {
    getTargetLabel(target) {
      return REPORT_TARGET_LABELS[target] || '全部对象'
    },
    
    getTypeLabel(type) {
      return REPORT_TYPE_LABELS[type] || '全部类型'
    },
    
    getStatusLabel(status) {
      return REPORT_STATUS_LABELS[status] || '全部状态'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/components/filter-bar.scss';

.filter-bar {
  margin-bottom: 24px;

  .filter-controls {
    display: flex;
    gap: 12px;
  }
}
</style>