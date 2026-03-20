<template>
  <div class="stats-row">
    <div
      v-for="card in cards"
      :key="card.status"
      class="stat-card"
      :class="{ active: filterStatus === card.status }"
      @click="$emit('update:filterStatus', card.status)"
    >
      <div class="stat-icon" :class="card.iconBg">
        <i :class="card.icon"></i>
      </div>
      <div class="stat-info">
        <span class="stat-label">{{ card.label }}</span>
        <span class="stat-value" :class="card.valueClass">{{ card.count }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatCards',
  props: {
    cards: {
      type: Array,
      required: true
    },
    filterStatus: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/components/stat-cards.scss';
</style>
