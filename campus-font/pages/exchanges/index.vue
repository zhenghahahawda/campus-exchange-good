<template>
  <div class="exchanges-page">
    <div class="page-header">
      <h1 class="title">我的换物</h1>
      <p class="subtitle">管理您的换物申请和记录</p>
    </div>

    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="进行中" name="ongoing">
        <div v-if="loading" class="loading-state">
          <i class="el-icon-loading"></i>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="ongoingExchanges.length > 0" class="exchange-list">
          <div v-for="item in ongoingExchanges" :key="item.orderId" class="exchange-card">
            <div class="card-header">
              <span class="status-tag" :class="getExchangeStatusClass(item.status)">{{ getExchangeStatusText(item.status) }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <div class="card-body">
              <img :src="item.goodsImage" class="goods-img" />
              <div class="info">
                <h3 class="goods-title">{{ item.goodsTitle }}</h3>
                <div class="seller-row">
                  <img :src="item.sellerAvatar" class="seller-avatar" />
                  <span class="seller-name">{{ item.otherUserName }}</span>
                  <span class="role-badge" v-if="item.isSender">我发起</span>
                  <span class="role-badge receiver" v-if="item.isReceiver">我收到</span>
                </div>
                <p class="exchange-message" v-if="item.message">{{ item.message }}</p>
              </div>
              <div class="actions-col">
                <!-- 发起方看到的按钮 -->
                <template v-if="item.isSender">
                  <el-button type="info" size="small" round icon="el-icon-document" @click.stop="viewGoodsDetail(item)">查看商品</el-button>
                  <el-button type="primary" size="small" round icon="el-icon-chat-dot-round" @click.stop="goToChat(item)">联系</el-button>
                  <el-button type="danger" size="small" round icon="el-icon-circle-close" @click.stop="cancelExchange(item)">取消</el-button>
                </template>
                
                <!-- 接收方看到的按钮 -->
                <template v-if="item.isReceiver">
                  <el-button type="info" size="small" round icon="el-icon-document" @click.stop="viewGoodsDetail(item)">查看商品</el-button>
                  <el-button type="warning" size="small" round icon="el-icon-s-check" @click.stop="handleExchange(item)">处理</el-button>
                  <el-button type="primary" size="small" round icon="el-icon-chat-dot-round" @click.stop="goToChat(item)">联系</el-button>
                </template>
              </div>
            </div>
            
            <!-- 凭证码确认区域（to_process / processing 状态） -->
            <div v-if="item.status === 'to_process' || item.status === 'processing'" class="confirm-section">
              <!-- 我的凭证码：给对方看，让对方输入 -->
              <div class="my-code-box" v-if="item.myExchangeCode">
                <span class="code-label">我的凭证码（告诉对方）：</span>
                <span class="code-value">{{ item.myExchangeCode }}</span>
                <el-button type="text" size="mini" icon="el-icon-copy-document" @click.stop="copyCode(item.myExchangeCode)">复制</el-button>
              </div>
              
              <div class="confirm-row">
                <div class="confirm-status">
                  <span v-if="item.isSender">
                    <i :class="item.initiatorConfirmed ? 'el-icon-circle-check confirmed' : 'el-icon-time pending-icon'"></i>
                    {{ item.initiatorConfirmed ? '你已确认收货' : '等待你确认收货' }}
                  </span>
                  <span v-if="item.isReceiver">
                    <i :class="item.receiverConfirmed ? 'el-icon-circle-check confirmed' : 'el-icon-time pending-icon'"></i>
                    {{ item.receiverConfirmed ? '你已确认收货' : '等待你确认收货' }}
                  </span>
                </div>
                <el-button
                  v-if="(item.isSender && !item.initiatorConfirmed) || (item.isReceiver && !item.receiverConfirmed)"
                  type="success" size="small" round icon="el-icon-key"
                  @click.stop="confirmExchange(item)"
                >输入对方凭证码确认收货</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">🔄</div>
          <h3>暂无进行中的换物</h3>
          <p>快去探索大厅看看有没有心仪的物品吧</p>
          <el-button type="primary" round @click="$router.push('/goods')">前往探索</el-button>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="已完成" name="completed">
        <div v-if="loadingCompleted" class="loading-state">
          <i class="el-icon-loading"></i>
          <p>加载中...</p>
        </div>

        <div v-else-if="completedExchanges.length > 0" class="exchange-list">
          <div v-for="item in completedExchanges" :key="item.orderId" class="exchange-card completed-card">
            <div class="card-header">
              <span class="status-tag" :class="item.status === 'cancelled' ? 'cancelled' : 'completed'">
                {{ item.status === 'cancelled' ? '已取消' : '已完成' }}
              </span>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <div class="card-body">
              <img :src="item.goodsImage" class="goods-img" />
              <div class="info">
                <h3 class="goods-title">{{ item.goodsTitle }}</h3>
                <div class="seller-row">
                  <img :src="item.sellerAvatar" class="seller-avatar" />
                  <span class="seller-name">{{ item.otherUserName }}</span>
                  <span class="role-badge" v-if="item.isSender">我发起</span>
                  <span class="role-badge receiver" v-if="item.isReceiver">我收到</span>
                </div>
                <p class="exchange-message" v-if="item.completedAt">完成时间：{{ item.completedAt }}</p>
              </div>
              <div class="actions-col">
                <el-button type="info" size="small" round icon="el-icon-document" @click.stop="viewGoodsDetail(item)">查看商品</el-button>
                <template v-if="item.status === 'completed'">
                  <el-button type="warning" size="small" round icon="el-icon-edit" @click.stop="openAppeal(item)" :disabled="item.appealed">申诉</el-button>
                  <el-button type="success" size="small" round icon="el-icon-star-off" @click.stop="openReview(item)">评价</el-button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">✅</div>
          <h3>暂无完成记录</h3>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="我发布的" name="my-posts">
        <div v-if="loadingMyGoods" class="loading-state">
          <i class="el-icon-loading"></i>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="myGoods.length > 0" class="goods-grid">
          <div v-for="item in myGoods" :key="item.good_id" class="goods-card" @click="goToGoodsDetail(item.good_id)">
            <div class="goods-image">
              <img :src="getFirstImage(item.images)" :alt="item.good_name" />
              <div class="status-badge" :class="getStatusClass(item)">
                {{ getStatusText(item) }}
              </div>
            </div>
            <div class="goods-info">
              <h3 class="goods-title">{{ item.good_name }}</h3>
              <p class="goods-desc">{{ item.description }}</p>
              <div class="goods-meta">
                <span class="time">{{ formatTime(item.created_at) }}</span>
                <span class="views"><i class="el-icon-view"></i> {{ item.view_count }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">📦</div>
          <h3>您还没有发布过物品</h3>
          <el-button type="primary" round icon="el-icon-plus" @click="showPublishDialog = true">立即发布</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 发布商品弹窗 -->
    <PublishGoodsDialog :visible.sync="showPublishDialog" />

    <!-- 评价弹窗 -->
    <el-dialog title="交易评价" :visible.sync="showReviewDialog" width="460px" :lock-scroll="false" append-to-body>
      <el-form ref="reviewForm" :model="reviewForm" :rules="reviewRules" label-width="80px" size="small">
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3"
            placeholder="分享你的交易体验" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="配图">
          <div class="review-imgs">
            <div v-for="(url, i) in reviewImages" :key="i" class="review-thumb">
              <img :src="url" />
              <span class="remove-btn" @click="reviewImages.splice(i, 1)"><i class="el-icon-close"></i></span>
            </div>
            <div v-if="reviewImages.length < 3" class="review-upload" @click="triggerReviewUpload">
              <i v-if="!reviewUploading" class="el-icon-plus"></i>
              <i v-else class="el-icon-loading"></i>
            </div>
          </div>
          <input ref="reviewFileInput" type="file" accept="image/*" style="display:none" @change="handleReviewImage" />
          <div style="font-size:12px;color:rgba(255,255,255,0.3);margin-top:4px">最多3张</div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="showReviewDialog = false">取消</el-button>
        <el-button type="success" size="small" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
      </span>
    </el-dialog>
    
    <!-- 商品图片预览对话框 -->
    <el-dialog
      :visible.sync="showGoodsImageDialog"
      :title="currentGoodsTitle"
      width="80%"
      center
      :modal="false"
      custom-class="goods-image-dialog"
    >
      <div class="goods-images-container">
        <el-carousel 
          v-if="currentGoodsImages.length > 1" 
          :interval="4000" 
          type="card" 
          height="500px"
          indicator-position="outside"
        >
          <el-carousel-item v-for="(image, index) in currentGoodsImages" :key="index">
            <img :src="image" :alt="`商品图片 ${index + 1}`" class="carousel-image" />
          </el-carousel-item>
        </el-carousel>
        
        <div v-else class="single-image-container">
          <img :src="currentGoodsImages[0]" alt="商品图片" class="single-image" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ExchangesPage',
  components: {
    PublishGoodsDialog: () => import('@/components/goods/publish/PublishGoodsDialog.vue')
  },
  data() {
    return {
      activeTab: 'ongoing',
      loading: false,
      ongoingExchanges: [],
      completedExchanges: [],
      loadingCompleted: false,
      myGoods: [],
      loadingMyGoods: false,
      showPublishDialog: false,
      currentUserId: null,
      // 商品图片预览
      showGoodsImageDialog: false,
      currentGoodsImages: [],
      currentGoodsTitle: '',
      // 评价
      showReviewDialog: false,
      reviewSubmitting: false,
      reviewUploading: false,
      reviewImages: [],
      currentReviewItem: null,
      reviewForm: { rating: 5, content: '' },
      reviewRules: {
        rating: [{ required: true, message: '请评分', trigger: 'change' }],
        content: [{ required: true, message: '请填写评价内容', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    console.log('🚀 exchanges页面已挂载');
    // 获取当前用户ID
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo);
        this.currentUserId = user.userId;
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    this.loadExchanges();
    this.loadMyGoods();
  },
  watch: {
    activeTab(newTab) {
      if (newTab === 'my-posts' && this.myGoods.length === 0) {
        this.loadMyGoods();
      }
      if (newTab === 'completed' && this.completedExchanges.length === 0) {
        this.loadCompletedExchanges();
      }
    }
  },
  methods: {
    async loadExchanges() {
      this.loading = true;
      try {
        console.log('📋 开始加载交换记录...');
        const response = await this.$axios.get('/exchanges');
        console.log('✅ API响应:', response);
        
        if (response.code === 20000 && Array.isArray(response.data)) {
          this.ongoingExchanges = response.data
            .filter(item => ['pending', 'to_process', 'processing'].includes(item.status))
            .map(item => {
              // 判断当前用户是发起方还是接收方
              const isSender = item.requesterId === this.currentUserId || item.initiatorId === this.currentUserId;
              const isReceiver = item.sellerId === this.currentUserId || item.receiverId === this.currentUserId;
              
              return {
                orderId: item.orderId,
                orderNumber: item.orderNumber,
                goodsId: item.goodsId,
                goodsTitle: `商品 #${item.goodsId}`,
                goodsImage: 'https://via.placeholder.com/300x200?text=加载中',
                sellerId: item.sellerId || item.receiverId,
                requesterId: item.requesterId || item.initiatorId,
                sellerName: `用户 #${item.sellerId || item.receiverId}`,
                sellerAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                status: item.status,
                createTime: this.formatTime(item.createdAt),
                message: item.remark || item.message || '无备注',
                // 标记用户角色
                isSender: isSender,
                isReceiver: isReceiver,
                // 显示对方信息
                otherUserId: isSender ? (item.sellerId || item.receiverId) : (item.requesterId || item.initiatorId),
                otherUserName: isSender ? `卖家 #${item.sellerId || item.receiverId}` : `买家 #${item.requesterId || item.initiatorId}`,
                // 凭证确认状态
                initiatorConfirmed: item.initiatorConfirmed || false,
                receiverConfirmed: item.receiverConfirmed || false,
                // 我的商品ID（用于获取我的凭证码）
                myGoodsId: isSender ? item.initiatorGoodId : item.receiverGoodId,
                myExchangeCode: null  // 异步加载
              };
            });
          
          console.log('✅ 加载了', this.ongoingExchanges.length, '条记录');
          
          // 异步加载商品详情
          this.loadGoodsDetails();
        }
      } catch (error) {
        console.error('❌ 加载失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    async loadCompletedExchanges() {
      this.loadingCompleted = true;
      try {
        const response = await this.$axios.get('/exchanges');
        if (response.code === 20000 && Array.isArray(response.data)) {
          this.completedExchanges = response.data
            .filter(item => ['completed', 'cancelled'].includes(item.status))
            .map(item => {
              const isSender = item.requesterId === this.currentUserId || item.initiatorId === this.currentUserId;
              const isReceiver = item.sellerId === this.currentUserId || item.receiverId === this.currentUserId;
              return {
                orderId: item.orderId,
                goodsId: item.goodsId,
                goodsTitle: `商品 #${item.goodsId}`,
                goodsImage: 'https://via.placeholder.com/300x200?text=已完成',
                sellerAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                status: item.status,
                createTime: this.formatTime(item.createdAt),
                completedAt: item.completedAt ? this.formatTime(item.completedAt) : '',
                isSender,
                isReceiver,
                otherUserName: isSender
                  ? `对方 #${item.sellerId || item.receiverId}`
                  : `对方 #${item.requesterId || item.initiatorId}`
              };
            });

          // 异步加载商品图片
          for (let ex of this.completedExchanges) {
            try {
              const res = await this.$axios.get(`/goods/${ex.goodsId}`);
              if (res.code === 20000 && res.data) {
                ex.goodsTitle = res.data.goodName || res.data.good_name || ex.goodsTitle;
                let imgs = res.data.images;
                if (typeof imgs === 'string') { try { imgs = JSON.parse(imgs); } catch(e) { imgs = [imgs]; } }
                if (Array.isArray(imgs) && imgs.length > 0) ex.goodsImage = imgs[0];
                this.$forceUpdate();
              }
            } catch(e) {}
          }
        }
      } catch (error) {
        console.error('加载已完成订单失败:', error);
      } finally {
        this.loadingCompleted = false;
      }
    },

    async loadGoodsDetails() {      for (let exchange of this.ongoingExchanges) {
        try {
          const response = await this.$axios.get(`/goods/${exchange.goodsId}`);
          if (response.code === 20000 && response.data) {
            const goods = response.data;
            exchange.goodsTitle = goods.goodName || goods.good_name || `商品 #${exchange.goodsId}`;
            
            let images = goods.images;
            if (typeof images === 'string') {
              try { images = JSON.parse(images); } catch (e) { images = [images]; }
            }
            if (Array.isArray(images) && images.length > 0) {
              exchange.goodsImage = images[0];
            }
          }
        } catch (error) {
          console.warn(`加载商品 ${exchange.goodsId} 详情失败:`, error);
        }
        
        // 加载我的商品凭证码（to_process/processing 状态才需要）
        if ((exchange.status === 'to_process' || exchange.status === 'processing') && exchange.myGoodsId) {
          try {
            const res = await this.$axios.get(`/goods/${exchange.myGoodsId}`);
            if (res.code === 20000 && res.data) {
              exchange.myExchangeCode = res.data.exchangeCode || res.data.exchange_code || null;
            }
          } catch (e) {
            console.warn('获取凭证码失败:', e);
          }
        }
        
        this.$forceUpdate();
      }
    },
    
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (hours < 1) return '刚刚';
      if (hours < 24) return `${hours}小时前`;
      if (days < 7) return `${days}天前`;
      return date.toLocaleDateString();
    },
    
    async loadMyGoods() {
      this.loadingMyGoods = true;
      try {
        console.log('📦 加载我的商品...');
        const response = await this.$axios.get('/goods/my', {
          params: {
            page: 1,
            limit: 100
          }
        });
        
        if (response.code === 20000 && response.data) {
          this.myGoods = (response.data.list || response.data || []).map(item => ({
            ...item,
            good_id: item.goodId || item.good_id,
            good_name: item.goodName || item.good_name,
            category_id: item.categoryId || item.category_id,
            condition_level: item.conditionLevel || item.condition_level,
            shelf_status: item.shelfStatus !== undefined ? item.shelfStatus : item.shelf_status,
            view_count: item.viewCount !== undefined ? item.viewCount : item.view_count,
            created_at: item.createdAt || item.created_at,
            status: item.status
          }));
          console.log(`✅ 加载了 ${this.myGoods.length} 件商品`);
        }
      } catch (error) {
        console.error('❌ 加载我的商品失败:', error);
      } finally {
        this.loadingMyGoods = false;
      }
    },
    
    getFirstImage(images) {
      if (!images) return 'https://via.placeholder.com/300x200?text=No+Image';
      if (typeof images === 'string') {
        try {
          const parsed = JSON.parse(images);
          return Array.isArray(parsed) && parsed.length > 0 ? parsed[0] : images;
        } catch {
          return images;
        }
      }
      if (Array.isArray(images) && images.length > 0) {
        return images[0];
      }
      return 'https://via.placeholder.com/300x200?text=No+Image';
    },
    
    getExchangeStatusClass(status) {
      const map = { pending: 'pending', to_process: 'processing', processing: 'processing', completed: 'completed', cancelled: 'cancelled' }
      return map[status] || 'pending'
    },

    getExchangeStatusText(status) {
      const map = { pending: '申请中', to_process: '待交换', processing: '交换中', completed: '已完成', cancelled: '已取消' }
      return map[status] || status
    },

    getStatusClass(item) {
      if (item.status === 0) return 'pending';
      if (item.status === 2) return 'rejected';
      const shelf = item.shelf_status !== undefined ? item.shelf_status : item.shelfStatus;
      if (shelf === 0) return 'off-shelf';
      if (shelf === 2) return 'off-shelf';
      return 'on-shelf';
    },
    
    getStatusText(item) {
      if (item.status === 0) return '待审核';
      if (item.status === 2) return '已拒绝';
      const shelf = item.shelf_status !== undefined ? item.shelf_status : item.shelfStatus;
      if (shelf === 0) return '已下架';
      if (shelf === 2) return '已售出';
      return '在售';
    },
    
    goToGoodsDetail(goodId) {
      if (!goodId) {
        console.error('商品ID不存在');
        this.$message.error('商品ID无效');
        return;
      }
      this.$router.push(`/goods/${goodId}`);
    },
    
    async goToChat(item) {
      try {
        // 尝试创建或获取与该订单关联的群聊会话
        const res = await this.$axios.post('/chat/conversations', {
          participantId: item.otherUserId,
          goodsId: item.goodsId,
          orderId: item.orderId,
          type: 'group'
        });

        if (res.code === 20000 && res.data) {
          const convId = res.data.id || res.data.conversationId;
          this.$router.push({ path: '/chat', query: { conversationId: convId } });
          return;
        }
      } catch (e) {
        // 接口不存在或失败，降级到 sessionStorage 方式
        console.warn('创建群聊失败，使用本地方式:', e);
      }

      // 降级：通过 sessionStorage 传递数据
      const chatData = {
        newChat: 'true',
        id: item.chatId || item.orderId,
        name: `交换：${item.goodsTitle}`,
        type: 'group',
        avatar: '',
        goodsTitle: item.goodsTitle,
        goodsStatus: item.status,
        members: [
          { name: '我', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png' },
          { name: item.otherUserName, avatar: item.sellerAvatar }
        ]
      };
      sessionStorage.setItem('pendingChatData', JSON.stringify(chatData));
      this.$router.push({ path: '/chat', query: { init: 'true' } });
    },
    
    endExchange(item) {
      this.$confirm('确定要结束这个换物申请吗？这将同时关闭对应的群聊。', '结束换物', {
        confirmButtonText: '确定结束',
        cancelButtonText: '取消',
        showClose: false,
        type: ''
      }).then(() => {
        // 1. 从进行中移除
        this.ongoingExchanges = this.ongoingExchanges.filter(e => e.orderId !== item.orderId);
        localStorage.setItem('myExchanges', JSON.stringify(this.ongoingExchanges));
        
        // 2. 添加到已完成 (模拟)
        let completed = [];
        try {
          const stored = localStorage.getItem('completedExchanges');
          if (stored) completed = JSON.parse(stored);
        } catch (e) {}
        
        item.status = 'completed';
        item.endTime = new Date().toLocaleString();
        completed.unshift(item);
        localStorage.setItem('completedExchanges', JSON.stringify(completed));
        
        // 3. 通知 Chat 页面关闭/归档该群聊 (通过 localStorage 标记)
        let closedChats = [];
        try {
          const stored = localStorage.getItem('closedChats');
          if (stored) closedChats = JSON.parse(stored);
        } catch (e) {}
        closedChats.push(item.chatId || item.orderId);
        localStorage.setItem('closedChats', JSON.stringify(closedChats));
        
        this.$message({
          message: '换物已结束',
          type: 'success',
          duration: 500,
          customClass: 'dark-toast'
        });
      }).catch(() => {});
    },
    
    // 查看商品详情
    async viewGoodsDetail(item) {
      if (!item.goodsId) {
        this.$message.error('商品ID无效');
        return;
      }
      
      try {
        // 获取商品详情
        const response = await this.$axios.get(`/goods/${item.goodsId}`);
        if (response.code === 20000 && response.data) {
          const goods = response.data;
          this.currentGoodsTitle = goods.goodName || goods.good_name || '商品详情';
          
          // 处理图片
          let images = goods.images;
          if (typeof images === 'string') {
            try {
              images = JSON.parse(images);
            } catch (e) {
              images = [images];
            }
          }
          
          if (Array.isArray(images) && images.length > 0) {
            this.currentGoodsImages = images;
          } else {
            this.currentGoodsImages = ['https://via.placeholder.com/600x400?text=暂无图片'];
          }
          
          // 显示对话框
          this.showGoodsImageDialog = true;
        }
      } catch (error) {
        console.error('获取商品详情失败:', error);
        this.$message.error('获取商品信息失败');
      }
    },
    
    // 处理交换申请（卖家操作）
    handleExchange(item) {
      this.$confirm('请选择操作', '处理交换申请', {
        distinguishCancelAndClose: true,
        confirmButtonText: '同意',
        cancelButtonText: '拒绝',
        type: 'info',
        center: true,
        customClass: 'exchange-handle-dialog'
      }).then(() => {
        // 点击"同意"
        this.acceptExchange(item);
      }).catch((action) => {
        // 点击"拒绝"
        if (action === 'cancel') {
          this.rejectExchange(item);
        }
      });
    },
    
    // 同意交换（卖家操作）
    async acceptExchange(item) {
      try {
        const response = await this.$axios.put(`/exchanges/${item.orderId}/status`, {
          status: 'to_process'
        })
        
        if (response.code === 20000) {
          this.$message.success('已同意交换申请')
          await this.loadExchanges()
        }
      } catch (error) {
        console.error('同意交换失败:', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },
    
    // 拒绝交换（卖家操作）
    async rejectExchange(item) {
      try {
        const response = await this.$axios.put(`/exchanges/${item.orderId}/status`, {
          status: 'cancelled'
        });
        
        if (response.code === 20000) {
          this.$message.success('已拒绝交换申请');
          // 从列表中移除
          this.ongoingExchanges = this.ongoingExchanges.filter(e => e.orderId !== item.orderId);
        }
      } catch (error) {
        console.error('拒绝交换失败:', error);
        this.$message.error('操作失败，请稍后重试');
      }
    },
    
    // 复制凭证码
    copyCode(code) {
      navigator.clipboard.writeText(code).then(() => {
        this.$message.success('凭证码已复制')
      }).catch(() => {
        this.$message.info(`凭证码：${code}`)
      })
    },

    // 输入凭证码确认收货
    async confirmExchange(item) {
      try {
        const { value: code } = await this.$prompt('请输入对方商品的凭证码', '确认收货', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          inputPlaceholder: '请输入凭证码'
        })
        if (!code) return
        
        const res = await this.$axios.post(`/exchanges/${item.orderId}/confirm`, null, {
          params: { exchangeCode: code }
        })
        
        if (res.code === 20000) {
          this.$message.success(res.message || '确认成功')
          // 刷新列表
          await this.loadExchanges()
        }
      } catch (e) {
        // 用户取消输入框不报错
        if (e !== 'cancel') {
          this.$message.error('确认失败，请检查凭证码是否正确')
        }
      }
    },

    // 取消交换（买家操作）
    async cancelExchange(item) {      this.$confirm('确定要取消这个交换申请吗？', '取消交换', {
        confirmButtonText: '确定取消',
        cancelButtonText: '我再想想',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await this.$axios.put(`/exchanges/${item.orderId}/status`, {
            status: 'cancelled'
          });
          
          if (response.code === 20000) {
            this.$message.success('已取消交换申请');
            this.ongoingExchanges = this.ongoingExchanges.filter(e => e.orderId !== item.orderId);
          }
        } catch (error) {
          console.error('取消交换失败:', error);
          this.$message.error('操作失败，请稍后重试');
        }
      }).catch(() => {});
    },

    // 申诉 - 直接确认后移到进行中
    openAppeal(item) {
      if (item.appealed) return
      this.$confirm('确认发起申诉？', '发起申诉', {
        confirmButtonText: '确认申诉',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.put(`/exchanges/${item.orderId}/status`, { status: 'processing' })
        } catch (e) {
          // 接口失败也继续本地处理
        }
        // 从已完成移除，加入进行中
        this.$set(item, 'appealed', true)
        this.completedExchanges = this.completedExchanges.filter(e => e.orderId !== item.orderId)
        item.status = 'processing'
        this.ongoingExchanges.unshift(item)
        this.activeTab = 'ongoing'
        this.$message.success('申诉已发起')
      }).catch(() => {})
    },

    // 评价
    openReview(item) {
      this.currentReviewItem = item
      this.reviewForm = { rating: 5, content: '' }
      this.reviewImages = []
      this.$nextTick(() => {
        this.$refs.reviewForm && this.$refs.reviewForm.clearValidate()
      })
      this.showReviewDialog = true
    },
    triggerReviewUpload() {
      if (this.reviewUploading) return
      this.$refs.reviewFileInput.value = ''
      this.$refs.reviewFileInput.click()
    },
    async handleReviewImage(e) {
      const file = e.target.files[0]
      if (!file) return
      if (file.size > 5 * 1024 * 1024) { this.$message.error('图片不能超过5MB'); return }
      this.reviewUploading = true
      try {
        const fd = new FormData()
        fd.append('file', file)
        const res = await this.$axios.post('/violations/upload-evidence', fd, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (res.code === 20000 && res.data) this.reviewImages.push(res.data)
      } catch (e) {
        this.$message.error('图片上传失败')
      } finally {
        this.reviewUploading = false
      }
    },
    async submitReview() {
      const valid = await this.$refs.reviewForm.validate().catch(() => false)
      if (!valid) return
      this.reviewSubmitting = true
      try {
        await this.$axios.post('/reviews', {
          orderId: this.currentReviewItem.orderId,
          targetUserId: this.currentReviewItem.otherUserId,
          rating: this.reviewForm.rating,
          content: this.reviewForm.content,
          images: this.reviewImages.length ? this.reviewImages : undefined
        })
        this.$message.success('评价提交成功')
        this.showReviewDialog = false
      } catch (e) {
        const msg = e.message || ''
        if (msg.includes('已评价') || msg.includes('重复')) {
          this.$message.warning('你已经评价过这笔交易了')
        } else {
          this.$message.error('提交失败，请稍后重试')
        }
      } finally {
        this.reviewSubmitting = false
      }
    }
  },
  head() {
    return {
      title: '我的换物 - 校园换物平台'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.exchanges-page {
  padding: 20px 0;
}

.page-header {
  margin-bottom: 32px;
  .title { font-size: 28px; font-weight: 700; color: $color-text-primary; margin-bottom: 8px; }
  .subtitle { font-size: 15px; color: $color-text-secondary; }
}

.loading-state {
  text-align: center;
  padding: 100px 0;
  
  i {
    font-size: 48px;
    color: $color-primary;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    color: $color-text-secondary;
  }
}

.custom-tabs {
  ::v-deep .el-tabs__nav-wrap::after { display: none; }
  ::v-deep .el-tabs__active-bar { height: 3px; border-radius: 2px; }
  ::v-deep .el-tabs__item { 
    font-size: 16px; 
    font-weight: 500; 
    height: 50px; 
    line-height: 50px;
    color: rgba(255, 255, 255, 0.7);
    
    &.is-active {
      color: $color-primary;
      font-weight: 600;
    }
    
    &:hover {
      color: #ffffff;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  @include glass-card;
  border-radius: 24px;
  margin-top: 20px;
  
  .empty-icon { font-size: 64px; margin-bottom: 24px; }
  h3 { font-size: 20px; font-weight: 600; color: $color-text-primary; margin-bottom: 12px; }
  p { font-size: 14px; color: $color-text-secondary; margin-bottom: 24px; }
}

.exchange-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
  
  .exchange-card {
    @include glass-card;
    padding: 16px;
    border-radius: 16px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      
      .status-tag {
        font-size: 12px;
        padding: 4px 12px;
        border-radius: 12px;
        background: rgba(var(--color-secondary-warning-rgb), 0.1);
        color: var(--color-secondary-warning);
        
        &.processing {
          background: rgba(var(--color-primary-rgb), 0.1);
          color: var(--color-primary);
        }
        &.completed {
          background: rgba(var(--color-secondary-success-rgb), 0.1);
          color: var(--color-secondary-success);
        }
        &.cancelled {
          background: rgba(var(--color-text-disabled-rgb), 0.1);
          color: var(--color-text-disabled);
        }
      }
      
      .time {
        font-size: 12px;
        color: var(--color-text-disabled);
      }
    }
    
    .card-body {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .goods-img {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        object-fit: cover;
      }
      
      .info {
        flex: 1;
        
        .goods-title {
          font-size: 16px;
          font-weight: 600;
          color: var(--color-text-primary);
          margin: 0 0 8px 0;
        }
        
        .seller-row {
          display: flex;
          align-items: center;
          gap: 6px;
          
          .seller-avatar {
            width: 20px;
            height: 20px;
            border-radius: 50%;
          }
          
          .seller-name {
            font-size: 13px;
            color: var(--color-text-secondary);
          }
          
          .role-badge {
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 10px;
            background: rgba(var(--color-primary-rgb), 0.1);
            color: var(--color-primary);
            margin-left: 4px;
            
            &.receiver {
              background: rgba(var(--color-secondary-success-rgb), 0.1);
              color: var(--color-secondary-success);
            }
          }
        }
        
        .exchange-message {
          font-size: 12px;
          color: var(--color-text-disabled);
          margin: 6px 0 0 0;
          font-style: italic;
        }
      }
      
      .actions-col {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .el-button {
          margin-left: 0;
        }
      }
    }
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 27px;
  margin-top: 20px;
  
  .goods-card {
    @include glass-card;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    }
    
    .goods-image {
      position: relative;
      height: 220px;
      overflow: hidden;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
      }
      
      .status-badge {
        position: absolute;
        top: 14px;
        right: 14px;
        padding: 5px 14px;
        border-radius: 14px;
        font-size: 17px;
        font-weight: $font-weight-medium;
        backdrop-filter: blur(8px);
        
        &.pending {
          background: rgba(var(--color-secondary-warning-rgb), 0.9);
          color: white;
        }
        
        &.rejected {
          background: rgba(var(--color-secondary-danger-rgb), 0.9);
          color: white;
        }
        
        &.off-shelf {
          background: rgba(var(--color-text-disabled-rgb), 0.9);
          color: white;
        }
        
        &.on-shelf {
          background: rgba(var(--color-secondary-success-rgb), 0.9);
          color: white;
        }
      }
    }
    
    &:hover .goods-image img {
      transform: scale(1.05);
    }
    
    .goods-info {
      padding: 20px;
      
      .goods-title {
        font-size: 24px;
        font-weight: $font-weight-semibold;
        color: $color-text-primary;
        margin: 0 0 10px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .goods-desc {
        font-size: 20px;
        color: $color-text-secondary;
        margin: 0 0 14px 0;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .goods-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 19px;
        color: $color-text-disabled;
        
        .views {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }
}

// 商品图片预览对话框样式
::v-deep .goods-image-dialog {
  .el-dialog__header {
    padding: 20px;
    background: var(--color-bg-surface);
    border-bottom: 1px solid var(--color-border);
  }
  
  .el-dialog__title {
    font-size: 20px;
    font-weight: 600;
    color: var(--color-text-primary);
  }
  
  .el-dialog__body {
    padding: 30px;
    background: var(--color-bg-primary);
  }
  
  .goods-images-container {
    .el-carousel {
      .el-carousel__item {
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--color-bg-surface);
        border-radius: 12px;
        overflow: hidden;
        
        .carousel-image {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }
      
      .el-carousel__indicators {
        .el-carousel__button {
          background: rgba(255, 255, 255, 0.3);
        }
        
        .is-active .el-carousel__button {
          background: var(--color-primary);
        }
      }
    }
    
    .single-image-container {
      display: flex;
      justify-content: center;
      align-items: center;
      background: var(--color-bg-surface);
      border-radius: 12px;
      padding: 20px;
      
      .single-image {
        max-width: 100%;
        max-height: 600px;
        object-fit: contain;
        border-radius: 8px;
      }
    }
  }
}

// 凭证码确认区域
.confirm-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
  
  .my-code-box {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: rgba(var(--color-primary-rgb), 0.08);
    border-radius: 8px;
    border: 1px dashed rgba(var(--color-primary-rgb), 0.3);
    
    .code-label {
      font-size: 12px;
      color: $color-text-secondary;
      white-space: nowrap;
    }
    
    .code-value {
      font-size: 14px;
      font-weight: 600;
      color: $color-primary;
      letter-spacing: 2px;
      font-family: monospace;
    }
  }
  
  .confirm-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
  
  .confirm-status {
    font-size: 13px;
    color: $color-text-secondary;
    display: flex;
    align-items: center;
    gap: 6px;
    
    i {
      font-size: 15px;
      &.confirmed { color: var(--color-secondary-success); }
      &.pending-icon { color: var(--color-secondary-warning); }
    }
  }
}

// 处理交换对话框样式
::v-deep .exchange-handle-dialog {
  .el-message-box__header {
    padding: 20px 20px 10px;
  }
  
  .el-message-box__title {
    font-size: 18px;
    font-weight: 600;
  }
  
  .el-message-box__message {
    padding: 20px 0;
    font-size: 14px;
  }
  
  .el-message-box__btns {
    padding: 10px 20px 20px;
    
    .el-button--primary {
      background: var(--color-secondary-success);
      border-color: var(--color-secondary-success);
      
      &:hover {
        background: var(--color-secondary-success);
        border-color: var(--color-secondary-success);
        opacity: 0.8;
      }
    }
    
    .el-button--default {
      background: var(--color-secondary-danger);
      border-color: var(--color-secondary-danger);
      color: white;
      
      &:hover {
        background: var(--color-secondary-danger);
        border-color: var(--color-secondary-danger);
        opacity: 0.8;
      }
    }
  }
}

.review-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.review-thumb {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(255,255,255,0.15);
  img { width: 100%; height: 100%; object-fit: cover; }
  .remove-btn {
    position: absolute;
    top: 2px; right: 2px;
    width: 16px; height: 16px;
    border-radius: 50%;
    background: rgba(0,0,0,0.6);
    color: #fff;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer; font-size: 10px;
    &:hover { background: #f56c6c; }
  }
}

.review-upload {
  width: 64px; height: 64px;
  border-radius: 6px;
  border: 1px dashed rgba(255,255,255,0.25);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  color: rgba(255,255,255,0.4);
  i { font-size: 20px; }
  &:hover { border-color: var(--color-primary); color: var(--color-primary); }
}

</style>
