<template>
  <div class="filter-bar">
    <div class="search-wrapper">
      <el-input
        :placeholder="placeholder"
        :value="searchQuery"
        @input="$emit('update:searchQuery', $event)"
        prefix-icon="el-icon-search"
        clearable
        class="capsule-input"
      >
      </el-input>
    </div>
    
    <div class="sort-wrapper">
      <GlassDropdown @command="handleSortCommand">
        <template slot="trigger" slot-scope="{ isOpen }">
          <div class="filter-dropdown">
            <span>{{ sortLabel }}</span>
            <i class="el-icon-arrow-down" :class="{ 'rotate-180': isOpen }" />
          </div>
        </template>
        <GlassDropdownItem command="default">默认排序</GlassDropdownItem>
        <GlassDropdownItem command="time_desc">最新订单</GlassDropdownItem>
        <GlassDropdownItem command="time_asc">最早订单</GlassDropdownItem>
      </GlassDropdown>
    </div>
  </div>
</template>

<script>
import GlassDropdown from '@/components/ui/GlassDropdown.vue';
import GlassDropdownItem from '@/components/ui/GlassDropdownItem.vue';
import { ORDER_SORT_LABELS } from '@/utils/orderConstants';

export default {
  name: 'OrderFilterBar',
  components: {
    GlassDropdown,
    GlassDropdownItem
  },
  props: {
    searchQuery: {
      type: String,
      default: ''
    },
    sortBy: {
      type: String,
      default: 'default'
    },
    placeholder: {
      type: String,
      default: '搜索订单号/买家姓名/手机号/商品名称...'
    }
  },
  computed: {
    sortLabel() {
      return ORDER_SORT_LABELS[this.sortBy] || ORDER_SORT_LABELS.default;
    }
  },
  methods: {
    handleSortCommand(command) {
      this.$emit('update:sortBy', command);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/components/filter-bar.scss';
</style>
