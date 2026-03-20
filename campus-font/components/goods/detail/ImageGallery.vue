<template>
  <div class="image-gallery">
    <div class="main-image">
      <img :src="currentImage" :alt="title" />
    </div>
    <div class="thumbnails" v-if="images.length > 1">
      <div 
        v-for="(img, index) in images" 
        :key="index"
        class="thumbnail-item"
        :class="{ active: currentImage === img }"
        @click="currentImage = img"
      >
        <img :src="img" alt="thumbnail" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GoodsImageGallery',
  props: {
    images: {
      type: Array,
      required: true,
      default: () => []
    },
    title: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      currentImage: ''
    }
  },
  watch: {
    images: {
      immediate: true,
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          this.currentImage = newVal[0];
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.image-gallery {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .main-image {
    @include glass-card;
    width: 100%;
    height: 420px;
    position: relative;
    overflow: hidden;
    border-radius: 24px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
      transition: transform 0.3s ease;
      
      &:hover {
        transform: scale(1.05);
      }
    }
  }
  
  .thumbnails {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding-bottom: 4px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    &::-webkit-scrollbar-thumb {
      background: rgba(var(--color-primary-rgb), 0.2);
      border-radius: 2px;
    }
    
    .thumbnail-item {
      width: 80px;
      height: 80px;
      flex-shrink: 0;
      border-radius: 12px;
      overflow: hidden;
      cursor: pointer;
      border: 2px solid transparent;
      transition: all 0.2s ease;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      &:hover {
        opacity: 0.8;
      }
      
      &.active {
        border-color: var(--color-primary);
        box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
      }
    }
  }
}
</style>
