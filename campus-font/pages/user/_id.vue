<template>
  <div class="user-profile-page">
    <div v-if="loading" class="loading-state">
      <i class="el-icon-loading"></i>
      <p>加载中...</p>
    </div>

    <template v-else>
      <!-- Hero Banner -->
      <div class="profile-hero">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <div class="hero-avatar-wrap">
            <img :src="user.avatar || defaultAvatar" class="hero-avatar" @error="handleAvatarError" />
          </div>
          <div class="hero-info">
            <h1 class="hero-username">{{ user.username }}</h1>
            <div class="hero-tags">
              <span class="tag" :class="user.college ? 'verified' : 'unverified'">
                <i class="el-icon-medal-1"></i>
                {{ user.college ? '已认证学籍' : '未认证' }}
              </span>
              <span class="tag credit">
                <i class="el-icon-star-on"></i>
                信誉分 {{ user.creditScore || 0 }}
              </span>
              <span v-if="user.school" class="tag school">
                <i class="el-icon-office-building"></i>
                {{ user.school }}
              </span>
            </div>
          </div>
          <div class="hero-actions" v-if="!isOwnProfile">
            <el-button type="primary" icon="el-icon-chat-dot-round" @click="handleContact" round>
              联系 TA
            </el-button>
          </div>
        </div>
      </div>

      <!-- Stats Bar -->
      <div class="stats-bar">
        <div class="stat-item">
          <span class="value">{{ user.goodsCount || goods.length }}</span>
          <span class="label">发布商品</span>
        </div>
        <div class="divider"></div>
        <div class="stat-item">
          <span class="value">{{ user.exchangeCount || 0 }}</span>
          <span class="label">完成交换</span>
        </div>
        <div class="divider"></div>
        <div class="stat-item">
          <span class="value">{{ user.joinTime }}</span>
          <span class="label">加入时间</span>
        </div>
      </div>

      <!-- Goods Section -->
      <div class="goods-section">
        <div class="section-header">
          <h2>发布的商品</h2>
          <span class="count">共 {{ goods.length }} 件</span>
        </div>

        <div v-if="loadingGoods" class="loading-state">
          <i class="el-icon-loading"></i>
        </div>
        <div v-else-if="goods.length > 0" class="goods-grid">
          <div
            v-for="item in goods"
            :key="item.goodId || item.good_id"
            class="goods-card"
            @click="$router.push(`/goods/${item.goodId || item.good_id}`)"
          >
            <div class="goods-img">
              <img :src="getFirstImage(item.images)" :alt="item.goodName" @error="handleImgError" />
              <span class="shelf-badge" :class="getShelfClass(item)">{{ getShelfText(item) }}</span>
            </div>
            <div class="goods-info">
              <h4>{{ item.goodName || item.good_name }}</h4>
              <p class="desc">{{ item.description }}</p>
              <div class="exchange-for" v-if="item.exchangeFor || item.exchange_for">
                <i class="el-icon-refresh"></i>
                <span>{{ item.exchangeFor || item.exchange_for }}</span>
              </div>
              <div class="meta">
                <span class="time">{{ formatTime(item.createdAt) }}</span>
                <span class="views"><i class="el-icon-view"></i> {{ item.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <i class="el-icon-box"></i>
          <p>该用户暂未发布商品</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

export default {
  name: 'UserProfilePage',
  data() {
    return {
      loading: true,
      loadingGoods: false,
      user: {},
      goods: [],
      defaultAvatar: DEFAULT_AVATAR
    }
  },
  computed: {
    isOwnProfile() {
      try {
        const info = localStorage.getItem('userInfo')
        if (!info) return false
        return String(JSON.parse(info).userId) === String(this.$route.params.id)
      } catch { return false }
    }
  },
  mounted() {
    this.loadUserProfile()
  },
  methods: {
    async loadUserProfile() {
      this.loading = true
      try {
        const userId = this.$route.params.id
        const res = await this.$axios.get(`/user/${userId}`)
        if (res.code === 20000 && res.data) {
          const u = res.data
          this.user = {
            userId: u.userId,
            username: u.username,
            avatar: u.avatar || DEFAULT_AVATAR,
            school: u.school || '',
            email: u.email || '',
            college: u.college || false,
            creditScore: u.creditScore || 0,
            goodsCount: u.goodsCount || 0,
            exchangeCount: u.exchangeCount || 0,
            responseRate: u.responseRate || null,
            joinTime: this.formatJoinTime(u.createdAt || u.created_at || u.createTime || u.registerTime)
          }
        }
      } catch (e) {
        console.error('加载用户信息失败:', e)
        this.$message.error('用户不存在')
        this.$router.push('/goods')
      } finally {
        this.loading = false
        this.loadUserGoods()
      }
    },
    async loadUserGoods() {
      this.loadingGoods = true
      try {
        const userId = this.$route.params.id
        const res = await this.$axios.get('/goods', { params: { userId, page: 1, size: 50 } })
        if (res.code === 20000 && res.data) {
          const list = res.data.list || res.data
          // Only show on-shelf goods (shelfStatus === 1)
          this.goods = list.filter(item => {
            const shelf = item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status
            return shelf === 1
          })
        }
      } catch (e) {
        console.error('加载商品失败:', e)
      } finally {
        this.loadingGoods = false
      }
    },
    async handleContact() {
      try {
        const res = await this.$axios.post('/chat/conversations', { participantId: this.user.userId })
        if (res.code === 20000 && res.data) {
          this.$router.push(`/chat?conversationId=${res.data.id}`)
        }
      } catch (e) {
        this.$message.error('无法发起会话')
      }
    },
    getFirstImage(images) {
      if (!images) return 'https://via.placeholder.com/400x300'
      const arr = typeof images === 'string'
        ? (() => { try { return JSON.parse(images) } catch { return [images] } })()
        : images
      return Array.isArray(arr) && arr.length > 0 ? arr[0] : 'https://via.placeholder.com/400x300'
    },
    getShelfClass(item) {
      const s = item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status
      if (s === 1) return 'on-shelf'
      if (s === 2) return 'sold-out'
      return 'off-shelf'
    },
    getShelfText(item) {
      const s = item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status
      if (s === 2) return '已售出'
      if (s === 0) return '已下架'
      return '在售'
    },
    formatJoinTime(dateStr) {
      if (!dateStr) return '未知'
      const date = new Date(dateStr)
      return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}`
    },
    formatTime(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const days = Math.floor((new Date() - date) / 86400000)
      if (days < 1) return '今天'
      if (days < 7) return `${days}天前`
      if (days < 30) return `${Math.floor(days / 7)}周前`
      return date.toLocaleDateString()
    },
    handleAvatarError(e) { e.target.src = DEFAULT_AVATAR },
    handleImgError(e) { e.target.src = 'https://via.placeholder.com/400x300' }
  },
  head() {
    return { title: `${this.user.username || '用户'} 的主页` }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.user-profile-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

.loading-state {
  text-align: center;
  padding: 80px 0;
  color: $color-text-secondary;
  i { font-size: 40px; display: block; margin-bottom: 12px; }
}

// ── Hero Banner ──────────────────────────────────────────────
.profile-hero {
  position: relative;
  padding: 48px 40px 36px;
  overflow: hidden;
  margin-bottom: 0;

  .hero-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg,
      rgba(var(--color-primary-rgb), 0.25) 0%,
      rgba(var(--color-primary-rgb), 0.08) 50%,
      transparent 100%);
    backdrop-filter: blur(0px);
    border-bottom: 1px solid rgba(255,255,255,0.06);
  }

  .hero-content {
    position: relative;
    display: flex;
    align-items: center;
    gap: 28px;
    max-width: 1100px;
    margin: 0 auto;

    @media (max-width: 640px) {
      flex-direction: column;
      text-align: center;
    }
  }

  .hero-avatar-wrap {
    flex-shrink: 0;
    .hero-avatar {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      object-fit: cover;
      border: 4px solid rgba(var(--color-primary-rgb), 0.5);
      box-shadow: 0 8px 32px rgba(0,0,0,0.3);
    }
  }

  .hero-info {
    flex: 1;
    .hero-username {
      font-size: 28px;
      font-weight: 800;
      color: $color-text-primary;
      margin: 0 0 12px;
      letter-spacing: 0.5px;
    }
    .hero-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .tag {
        font-size: 12px;
        padding: 4px 12px;
        border-radius: 20px;
        display: inline-flex;
        align-items: center;
        gap: 5px;
        font-weight: 500;

        &.verified {
          background: rgba(var(--color-secondary-success-rgb), 0.15);
          color: var(--color-secondary-success);
          border: 1px solid rgba(var(--color-secondary-success-rgb), 0.3);
        }
        &.unverified {
          background: rgba(128,128,128,0.1);
          color: $color-text-secondary;
          border: 1px solid rgba(128,128,128,0.2);
        }
        &.credit {
          background: rgba(var(--color-primary-rgb), 0.15);
          color: var(--color-primary);
          border: 1px solid rgba(var(--color-primary-rgb), 0.3);
        }
        &.school {
          background: rgba(255,255,255,0.06);
          color: $color-text-secondary;
          border: 1px solid rgba(255,255,255,0.1);
        }
      }
    }
  }

  .hero-actions {
    flex-shrink: 0;
    .el-button { padding: 10px 28px; font-size: 14px; }
  }
}

// ── Stats Bar ────────────────────────────────────────────────
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 20px 40px;
  background: rgba(255,255,255,0.03);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  max-width: 100%;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    max-width: 200px;

    .value {
      font-size: 22px;
      font-weight: 700;
      color: $color-text-primary;
    }
    .label {
      font-size: 12px;
      color: $color-text-secondary;
    }
  }

  .divider {
    width: 1px;
    height: 36px;
    background: rgba(255,255,255,0.08);
    flex-shrink: 0;
  }
}

// ── Goods Section ────────────────────────────────────────────
.goods-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 36px 24px 0;

  .section-header {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 24px;

    h2 {
      font-size: 20px;
      font-weight: 700;
      color: $color-text-primary;
      margin: 0;
    }
    .count {
      font-size: 13px;
      color: $color-text-secondary;
    }
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.goods-card {
  @include glass-card;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0,0,0,0.3);
  }

  .goods-img {
    position: relative;
    height: 200px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .shelf-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      font-size: 11px;
      padding: 3px 10px;
      border-radius: 10px;
      font-weight: 600;

      &.on-shelf { background: rgba(var(--color-secondary-success-rgb), 0.9); color: #fff; }
      &.sold-out { background: rgba(100,100,100,0.85); color: #fff; }
      &.off-shelf { background: rgba(0,0,0,0.55); color: #fff; }
    }
  }

  .goods-info {
    padding: 14px 16px;

    h4 {
      font-size: 15px;
      font-weight: 600;
      color: $color-text-primary;
      margin: 0 0 6px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .desc {
      font-size: 13px;
      color: $color-text-secondary;
      margin: 0 0 8px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.5;
    }

    .exchange-for {
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 12px;
      color: var(--color-primary);
      margin-bottom: 8px;
      i { font-size: 13px; }
      span {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .meta {
      display: flex;
      justify-content: space-between;
      font-size: 11px;
      color: $color-text-disabled;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: $color-text-secondary;
  i { font-size: 56px; display: block; margin-bottom: 16px; opacity: 0.3; }
  p { font-size: 14px; }
}
</style>
