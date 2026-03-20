<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
            <h3>消息列表</h3>
          </div>
        
        <div class="conversation-list">
          <div 
            v-for="conv in conversations" 
            :key="conv.id"
            class="conversation-item"
            :class="{ active: currentConv.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <div class="avatar-wrapper">
                <div class="default-group-avatar" v-if="!conv.avatar">
                  <i class="el-icon-chat-dot-round"></i>
                </div>
                <img v-else :src="conv.avatar" :alt="conv.name" @error="handleConvImageError(conv)" />
                <div class="online-status" v-if="conv.online"></div>
              </div>
            <div class="conv-content">
              <div class="conv-top">
                <span class="conv-name">{{ conv.name }}</span>
                <span class="conv-time">{{ conv.lastTime }}</span>
              </div>
              <div class="conv-bottom">
                <p class="conv-msg">
                  <span v-if="conv.type === 'group'" class="group-tag">[群]</span>
                  {{ conv.lastMessage }}
                </p>
                <span class="unread-badge" v-if="conv.unread > 0">{{ conv.unread }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-main">
        <template v-if="currentConv.id">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="user-info">
              <span class="user-name">{{ currentConv.name }}</span>
              <span class="user-status" v-if="currentConv.online">在线</span>
            </div>
          </div>

          <!-- 交易信息面板（如果会话关联了交换申请） -->
          <div class="exchange-panel" v-if="exchangeInfo">
            <div class="exchange-goods-info">
              <img :src="exchangeInfo.goodsImage" alt="商品" class="goods-thumb" />
              <div class="goods-detail">
                <div class="goods-header">
                  <span class="goods-name">{{ exchangeInfo.goodsName }}</span>
                  <span class="role-badge" v-if="currentUserRole">
                    {{ currentUserRole === 'seller' ? '我是卖家' : '我想换' }}
                  </span>
                </div>
                <span class="exchange-status" :class="exchangeInfo.status">
                  {{ getExchangeStatusText(exchangeInfo.status) }}
                </span>
              </div>
            </div>
            
            <div class="exchange-actions" v-if="currentUserRole">
              <!-- 卖家视角 -->
              <template v-if="currentUserRole === 'seller'">
                <template v-if="exchangeInfo.status === 'pending'">
                  <el-button type="success" size="small" @click="handleAcceptExchange">接受交换</el-button>
                  <el-button type="danger" size="small" plain @click="handleRejectExchange">拒绝</el-button>
                </template>
                <template v-else-if="exchangeInfo.status === 'accepted'">
                  <span class="status-text">已接受，等待线下交换</span>
                  <el-button type="primary" size="small" @click="handleCompleteExchange">确认完成</el-button>
                </template>
                <template v-else-if="exchangeInfo.status === 'completed'">
                  <span class="success-text">✓ 交换已完成</span>
                </template>
                <template v-else-if="exchangeInfo.status === 'rejected'">
                  <span class="error-text">已拒绝此交换</span>
                </template>
              </template>
              
              <!-- 买家视角 -->
              <template v-if="currentUserRole === 'buyer'">
                <template v-if="exchangeInfo.status === 'pending'">
                  <span class="status-text">等待卖家回应...</span>
                  <el-button type="text" size="small" @click="handleCancelExchange">取消申请</el-button>
                </template>
                <template v-else-if="exchangeInfo.status === 'accepted'">
                  <span class="status-text">卖家已接受，可以线下交换了</span>
                  <el-button type="primary" size="small" @click="handleCompleteExchange">确认完成</el-button>
                </template>
                <template v-else-if="exchangeInfo.status === 'completed'">
                  <span class="success-text">✓ 交换已完成</span>
                </template>
                <template v-else-if="exchangeInfo.status === 'rejected'">
                  <span class="error-text">卖家拒绝了此交换</span>
                </template>
              </template>
            </div>
          </div>

          <!-- 消息列表 -->
          <div class="messages-area" ref="messagesArea">
            <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="{ 'is-me': msg.isMe, 'is-system': msg.isSystem, 'is-goods': msg.type === 'goods' }">
              <template v-if="msg.isSystem">
                <span class="system-msg">{{ msg.content }}</span>
              </template>
              
              <template v-else-if="msg.type === 'goods'">
                <div class="goods-bubble">
                  <div class="goods-header">
                    <span class="exchange-icon-circle">
                      <i class="el-icon-goods"></i>
                    </span>
                    <span class="exchange-label">交换</span>
                  </div>
                  <div class="goods-content">
                    <span class="goods-title">{{ msg.data.title }}</span>
                    <span class="status-dot" :class="msg.data.status"></span>
                  </div>
                </div>
              </template>
              
              <template v-else-if="msg.type === 'image'">
                <img :src="msg.isMe ? myAvatar : (msg.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')" class="msg-avatar" @error="handleImageError" />
                <div class="msg-content">
                  <span v-if="!msg.isMe && msg.senderName" class="msg-sender-name">{{ msg.senderName }}</span>
                  <div class="msg-bubble image-bubble">
                    <img :src="msg.content" alt="图片" class="chat-image" @click="previewImage(msg.content)" />
                  </div>
                  <span class="msg-time">{{ msg.time }}</span>
                </div>
              </template>
              
              <template v-else-if="msg.type === 'file'">
                <img :src="msg.isMe ? myAvatar : (msg.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')" class="msg-avatar" @error="handleImageError" />
                <div class="msg-content">
                  <span v-if="!msg.isMe && msg.senderName" class="msg-sender-name">{{ msg.senderName }}</span>
                  <div class="msg-bubble file-bubble" @click="downloadFile(msg.content)">
                    <i class="el-icon-document"></i>
                    <div class="file-info">
                      <span class="file-name">{{ getFileName(msg.content) }}</span>
                      <span class="file-size">{{ getFileSize(msg.content) }}</span>
                    </div>
                  </div>
                  <span class="msg-time">{{ msg.time }}</span>
                </div>
              </template>
              
              <template v-else-if="msg.type === 'location'">
                <img :src="msg.isMe ? myAvatar : (msg.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')" class="msg-avatar" @error="handleImageError" />
                <div class="msg-content">
                  <span v-if="!msg.isMe && msg.senderName" class="msg-sender-name">{{ msg.senderName }}</span>
                  <div class="msg-bubble location-bubble" @click="openLocation(msg.content)">
                    <i class="el-icon-location"></i>
                    <div class="location-info">
                      <span class="location-address">{{ getLocationAddress(msg.content) }}</span>
                      <span class="location-tip">点击查看地图</span>
                    </div>
                  </div>
                  <span class="msg-time">{{ msg.time }}</span>
                </div>
              </template>
              
              <template v-else>
                <img :src="msg.isMe ? myAvatar : (msg.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')" class="msg-avatar" @error="handleImageError" />
                <div class="msg-content">
                  <span v-if="!msg.isMe && msg.senderName" class="msg-sender-name">{{ msg.senderName }}</span>
                  <div class="msg-bubble">{{ msg.content }}</div>
                  <span class="msg-time">{{ msg.time }}</span>
                </div>
              </template>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <div class="toolbar">
              <input 
                type="file" 
                ref="imageInput" 
                accept="image/*" 
                style="display: none" 
                @change="handleImageSelect"
              />
              <i class="el-icon-picture-outline tool-icon" @click="$refs.imageInput.click()" title="发送图片"></i>
              
              <input 
                type="file" 
                ref="fileInput" 
                style="display: none" 
                @change="handleFileSelect"
              />
              <i class="el-icon-folder-opened tool-icon" @click="$refs.fileInput.click()" title="发送文件"></i>
              
              <i class="el-icon-location-outline tool-icon" @click="handleSendLocation" title="发送位置"></i>
            </div>
            <textarea 
              v-model="inputMessage" 
              placeholder="输入消息..." 
              @keyup.enter.exact.prevent="sendMessage"
            ></textarea>
            <div class="send-bar">
              <span class="tip">按 Enter 发送</span>
              <el-button type="primary" size="small" @click="sendMessage" :disabled="!inputMessage.trim()">发送</el-button>
            </div>
          </div>
        </template>
        
        <div v-else class="empty-chat">
          <i class="el-icon-chat-line-square"></i>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatPage',
  data() {
    return {
      myAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      inputMessage: '',
      currentConv: {},
      conversations: [],
      messages: [],
      // 交易状态（如果会话关联了商品）
      exchangeInfo: null, // 从后端获取的交易信息
      // 轮询定时器
      messagePollingTimer: null,
      conversationPollingTimer: null
    }
  },
  computed: {
    // 判断当前用户在交易中的角色
    currentUserRole() {
      if (!this.exchangeInfo) return null;
      const currentUserId = this.getCurrentUserId();
      if (this.exchangeInfo.sellerId === currentUserId) return 'seller';
      if (this.exchangeInfo.buyerId === currentUserId) return 'buyer';
      return null;
    }
  },
  mounted() {
    // 从 localStorage 加载当前用户真实头像
    try {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        const user = JSON.parse(userInfo);
        if (user.avatar) this.myAvatar = user.avatar;
      }
    } catch (e) {}
    
    this.loadConversations();
    this.handleNewChat();
    this.handleDirectConversation();
    this.checkClosedChats();
    
    // 启动轮询：每5秒检查新消息
    this.startMessagePolling();
    // 启动轮询：每30秒刷新会话列表
    this.startConversationPolling();
  },
  beforeDestroy() {
    // 清除轮询定时器
    this.stopMessagePolling();
    this.stopConversationPolling();
  },
  methods: {
    // --- API Methods ---
    async loadConversations() {
      try {
        console.log('📞 正在调用会话列表接口...');
        const response = await this.$axios.get('/chat/conversations');
        console.log('✅ 会话列表响应:', response);
        
        if (response.code === 20000 && response.data) {
          // 获取当前用户ID
          let currentUserId = null;
          try {
            const userInfo = localStorage.getItem('userInfo');
            if (userInfo) currentUserId = JSON.parse(userInfo).userId;
          } catch (e) {}
          
          this.conversations = response.data.map(conv => {
            // 找到对方用户信息（不是自己的那个participant）
            let displayName = conv.name;
            let displayAvatar = conv.avatar || '';
            
            // 判断是否群聊：type === 'group' 或名称包含 ORD- 格式
            const isGroup = conv.type === 'group' || /ORD-[\w-]+/.test(conv.name || '');

            if (isGroup) {
              const rawName = conv.name || '';
              const cleanName = rawName.replace(/\s*-\s*ORD-[\w-]+$/, '').trim();
              displayName = conv.goodsName
                ? `交换：${conv.goodsName}`
                : (cleanName || '交换群聊');
              // 群聊没有头像时用聊天图标（保持原样，通过 v-if 显示图标）
              displayAvatar = conv.avatar || '';
            }
            // 私聊：找对方用户信息
            else {
              const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
              // 如果有otherUser字段，优先使用
              if (conv.otherUser) {
                displayName = conv.otherUser.username || conv.otherUser.name || displayName;
                displayAvatar = conv.otherUser.avatar || conv.otherUser.avatarUrl || DEFAULT_AVATAR;
              }
              // 如果有participants数组，找到不是自己的那个
              else if (Array.isArray(conv.participants) && currentUserId) {
                const other = conv.participants.find(p => 
                  (p.userId || p.id) !== currentUserId && 
                  (p.userId || p.id) !== String(currentUserId)
                );
                if (other) {
                  displayName = other.username || other.name || displayName;
                  displayAvatar = other.avatar || other.avatarUrl || DEFAULT_AVATAR;
                }
              }
              // 兜底：如果还是没有头像，用默认
              if (!displayAvatar) displayAvatar = DEFAULT_AVATAR;
            }
            
            return {
              id: conv.id,
              name: displayName,
              avatar: displayAvatar,
              lastMessage: conv.lastMessage || '暂无消息',
              lastTime: this.formatTime(conv.lastTime),
              unread: conv.unread || 0,
              online: false,
              type: isGroup ? 'group' : (conv.type || 'private'),
              orderId: conv.orderId || null,
              members: conv.participants || []
            };
          });
          
          console.log(`✅ 加载了 ${this.conversations.length} 个会话`);
          
          // 默认选中第一个会话（如果没有通过URL参数指定）
          if (!this.currentConv.id && !this.$route.query.conversationId && this.conversations.length > 0) {
            this.selectConversation(this.conversations[0]);
          }
        } else {
          // 如果返回的数据为空，设置为空数组
          console.log('⚠️ 会话列表为空');
          this.conversations = [];
        }
      } catch (error) {
        console.error('❌ 加载会话列表失败:', error);
        console.error('错误详情:', {
          status: error.response?.status,
          statusText: error.response?.statusText,
          data: error.response?.data
        });
        this.conversations = [];
        
        if (error.response?.status === 401) {
          // 401 由 axios 拦截器统一处理
        } else if (error.response?.status === 404) {
          // 接口不存在，可能后端还没实现
          console.warn('💡 提示：聊天接口 GET /api/chat/conversations 暂未实现');
          console.warn('💡 请确保后端已实现该接口');
        } else {
          // 其他错误，显示提示但不阻断用户
          console.warn('暂时无法加载会话列表');
        }
      }
    },
    
    async loadMessages(conversationId) {
      try {
        const response = await this.$axios.get(`/chat/conversations/${conversationId}/messages`, {
          params: { page: 1, limit: 50 }
        });
        
        if (response.code === 20000 && response.data) {
          const currentUserId = this.getCurrentUserId();
          // 后端返回的消息是倒序的（最新在前），需要反转
          const newMessages = response.data.messages.reverse().map(msg => {
            let content = msg.content;
            // 清理系统消息或包含订单号的消息
            const isSystemLike = msg.type === 'system' || msg.isSystem || /ORD-[\w-]+/.test(content);
            if (isSystemLike) {
              content = content.replace(/交换订单\s*ORD-[\w-]+\s*已创建[，,]请在此群聊中协商交换事宜。?/, '我想跟你聊一聊');
              content = content.replace(/ORD-[\w-]+/g, '').trim();
            }
            return {
              id: msg.id,
              content,
              time: this.formatTime(msg.createdAt),
              isMe: msg.senderId === currentUserId,
              isSystem: msg.type === 'system' || msg.isSystem || false,
              type: msg.type || 'text',
              avatar: msg.sender?.avatar || '',
              senderName: msg.sender?.username || msg.sender?.name || ''
            };
          });
          
          // 检查是否有新消息
          const hasNewMessages = this.messages.length === 0 || 
            newMessages.length > this.messages.length ||
            (newMessages.length > 0 && this.messages.length > 0 && 
             newMessages[newMessages.length - 1].id !== this.messages[this.messages.length - 1].id);
          
          this.messages = newMessages;
          
          // 如果有新消息，滚动到底部
          if (hasNewMessages) {
            this.scrollToBottom();
          }
        }
      } catch (error) {
        console.error('加载消息失败:', error);
        // 不显示错误提示，避免打扰用户
      }
    },
    
    // 轮询方法
    startMessagePolling() {
      // 每5秒检查一次新消息
      this.messagePollingTimer = setInterval(() => {
        if (this.currentConv.id) {
          this.loadMessages(this.currentConv.id);
        }
      }, 5000);
    },
    
    stopMessagePolling() {
      if (this.messagePollingTimer) {
        clearInterval(this.messagePollingTimer);
        this.messagePollingTimer = null;
      }
    },
    
    startConversationPolling() {
      // 每30秒刷新会话列表
      this.conversationPollingTimer = setInterval(() => {
        this.loadConversations();
      }, 30000);
    },
    
    stopConversationPolling() {
      if (this.conversationPollingTimer) {
        clearInterval(this.conversationPollingTimer);
        this.conversationPollingTimer = null;
      }
    },
    
    async loadExchangeInfo(conversationId) {
      try {
        // 尝试获取与此会话关联的交换信息
        const response = await this.$axios.get(`/exchanges/conversation/${conversationId}`);
        
        if (response.code === 20000 && response.data) {
          this.exchangeInfo = {
            id: response.data.id,
            goodsId: response.data.goodsId,
            goodsName: response.data.goodsName || response.data.goods?.goodName,
            goodsImage: (() => {
              const images = response.data.goods?.images;
              if (typeof images === 'string') {
                try {
                  const parsed = JSON.parse(images);
                  return Array.isArray(parsed) && parsed.length > 0 ? parsed[0] : 'https://via.placeholder.com/48';
                } catch {
                  return 'https://via.placeholder.com/48';
                }
              }
              return Array.isArray(images) && images.length > 0 ? images[0] : 'https://via.placeholder.com/48';
            })(),
            status: response.data.status,
            sellerId: response.data.sellerId,
            buyerId: response.data.requesterId
          };
          console.log('✅ 加载交换信息成功:', this.exchangeInfo);
        } else {
          // 没有关联的交换信息
          this.exchangeInfo = null;
        }
      } catch (error) {
        // 如果返回404或其他错误，说明没有关联的交换或接口未实现
        this.exchangeInfo = null;
        
        // 只在非404错误时输出警告
        if (error.response?.status !== 404) {
          console.warn('获取交换信息失败:', error);
        }
      }
    },
    
    async sendMessageToBackend() {
      if (!this.inputMessage.trim() || !this.currentConv.id) return;
      
      try {
        const response = await this.$axios.post('/chat/messages', {
          conversationId: this.currentConv.id,
          content: this.inputMessage.trim(),
          type: 'text'
        });
        
        if (response.code === 20000 && response.data) {
          const newMsg = {
            id: response.data.id,
            content: this.inputMessage,
            time: this.formatTime(response.data.createdAt),
            isMe: true,
            isSystem: false,
            type: 'text'
          };
          
          this.messages.push(newMsg);
          this.inputMessage = '';
          this.scrollToBottom();
          
          // 更新会话列表中的最后消息
          const conv = this.conversations.find(c => c.id === this.currentConv.id);
          if (conv) {
            conv.lastMessage = newMsg.content;
            conv.lastTime = newMsg.time;
          }
        }
      } catch (error) {
        console.error('发送消息失败:', error);
        this.$message.error('发送失败，请重试');
      }
    },
    
    async handleImageSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('请选择图片文件');
        return;
      }
      
      // 验证文件大小（限制5MB）
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片大小不能超过5MB');
        return;
      }
      
      try {
        const loading = this.$message({
          message: '正在上传图片...',
          duration: 0,
          iconClass: 'el-icon-loading'
        });
        
        // 步骤1：上传图片
        const formData = new FormData();
        formData.append('file', file);
        
        const uploadResponse = await this.$axios.post('/goods/upload-image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        if (uploadResponse.code === 20000 && uploadResponse.data) {
          const imageUrl = uploadResponse.data;
          
          // 步骤2：发送图片消息
          const response = await this.$axios.post('/chat/messages', {
            conversationId: this.currentConv.id,
            content: imageUrl,
            type: 'image'
          });
          
          if (response.code === 20000 && response.data) {
            const newMsg = {
              id: response.data.id,
              content: imageUrl,
              time: this.formatTime(response.data.createdAt),
              isMe: true,
              isSystem: false,
              type: 'image'
            };
            
            this.messages.push(newMsg);
            this.scrollToBottom();
            
            // 更新会话列表
            const conv = this.conversations.find(c => c.id === this.currentConv.id);
            if (conv) {
              conv.lastMessage = '[图片]';
              conv.lastTime = newMsg.time;
            }
          }
        }
        
        loading.close();
        this.$refs.imageInput.value = '';
      } catch (error) {
        console.error('发送图片失败:', error);
        this.$message.error('发送图片失败');
        this.$refs.imageInput.value = '';
      }
    },
    
    async handleFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 验证文件大小（限制10MB）
      if (file.size > 10 * 1024 * 1024) {
        this.$message.error('文件大小不能超过10MB');
        return;
      }
      
      try {
        const loading = this.$message({
          message: '正在上传文件...',
          duration: 0,
          iconClass: 'el-icon-loading'
        });
        
        // 步骤1：上传文件（使用图片上传接口，后端应该支持所有文件类型）
        const formData = new FormData();
        formData.append('file', file);
        
        const uploadResponse = await this.$axios.post('/goods/upload-image', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        
        if (uploadResponse.code === 20000 && uploadResponse.data) {
          const fileUrl = uploadResponse.data;
          
          // 步骤2：发送文件消息
          const fileInfo = {
            url: fileUrl,
            name: file.name,
            size: file.size
          };
          
          const response = await this.$axios.post('/chat/messages', {
            conversationId: this.currentConv.id,
            content: JSON.stringify(fileInfo),
            type: 'file'
          });
          
          if (response.code === 20000 && response.data) {
            const newMsg = {
              id: response.data.id,
              content: JSON.stringify(fileInfo),
              time: this.formatTime(response.data.createdAt),
              isMe: true,
              isSystem: false,
              type: 'file'
            };
            
            this.messages.push(newMsg);
            this.scrollToBottom();
            
            // 更新会话列表
            const conv = this.conversations.find(c => c.id === this.currentConv.id);
            if (conv) {
              conv.lastMessage = '[文件]';
              conv.lastTime = newMsg.time;
            }
          }
        }
        
        loading.close();
        this.$refs.fileInput.value = '';
      } catch (error) {
        console.error('发送文件失败:', error);
        this.$message.error('发送文件失败');
        this.$refs.fileInput.value = '';
      }
    },
    
    async handleSendLocation() {
      // 简化版：使用浏览器地理位置API
      if (!navigator.geolocation) {
        this.$message.error('您的浏览器不支持地理定位');
        return;
      }
      
      const loading = this.$message({
        message: '正在获取位置...',
        duration: 0,
        iconClass: 'el-icon-loading'
      });
      
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            const locationInfo = {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
              address: '当前位置' // 实际项目中应该调用地图API获取地址
            };
            
            const response = await this.$axios.post('/chat/messages', {
              conversationId: this.currentConv.id,
              content: JSON.stringify(locationInfo),
              type: 'location'
            });
            
            if (response.code === 20000 && response.data) {
              const newMsg = {
                id: response.data.id,
                content: JSON.stringify(locationInfo),
                time: this.formatTime(response.data.createdAt),
                isMe: true,
                isSystem: false,
                type: 'location'
              };
              
              this.messages.push(newMsg);
              this.scrollToBottom();
              
              // 更新会话列表
              const conv = this.conversations.find(c => c.id === this.currentConv.id);
              if (conv) {
                conv.lastMessage = '[位置]';
                conv.lastTime = newMsg.time;
              }
            }
            
            loading.close();
          } catch (error) {
            console.error('发送位置失败:', error);
            loading.close();
            this.$message.error('发送位置失败');
          }
        },
        (error) => {
          loading.close();
          this.$message.error('获取位置失败：' + error.message);
        }
      );
    },
    
    // 辅助方法
    previewImage(url) {
      window.open(url, '_blank');
    },
    
    getFileName(content) {
      try {
        const fileInfo = JSON.parse(content);
        return fileInfo.name || '未知文件';
      } catch {
        return '未知文件';
      }
    },
    
    getFileSize(content) {
      try {
        const fileInfo = JSON.parse(content);
        const size = fileInfo.size || 0;
        if (size < 1024) return size + ' B';
        if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
        return (size / (1024 * 1024)).toFixed(1) + ' MB';
      } catch {
        return '';
      }
    },
    
    downloadFile(content) {
      try {
        const fileInfo = JSON.parse(content);
        window.open(fileInfo.url, '_blank');
      } catch (error) {
        this.$message.error('无法打开文件');
      }
    },
    
    getLocationAddress(content) {
      try {
        const locationInfo = JSON.parse(content);
        return locationInfo.address || '位置信息';
      } catch {
        return '位置信息';
      }
    },
    
    openLocation(content) {
      try {
        const locationInfo = JSON.parse(content);
        // 使用高德地图或百度地图打开位置
        const url = `https://uri.amap.com/marker?position=${locationInfo.longitude},${locationInfo.latitude}&name=${encodeURIComponent(locationInfo.address)}`;
        window.open(url, '_blank');
      } catch (error) {
        this.$message.error('无法打开地图');
      }
    },
    
    getCurrentUserId() {
      try {
        const userInfo = localStorage.getItem('userInfo');
        if (userInfo) {
          const user = JSON.parse(userInfo);
          return user.userId || user.id;
        }
      } catch (e) {
        console.error('获取用户ID失败', e);
      }
      return null;
    },
    
    formatTime(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const now = new Date();
      const diff = now - date;
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (hours < 1) return '刚刚';
      if (hours < 24) return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      if (days < 7) return `${days}天前`;
      return date.toLocaleDateString();
    },
    
    handleDirectConversation() {
      // 处理从商品详情页或换物页直接跳转过来的情况
      const conversationId = this.$route.query.conversationId;
      if (conversationId) {
        // 先在已加载的列表中找
        const conv = this.conversations.find(c => String(c.id) === String(conversationId));
        if (conv) {
          this.selectConversation(conv);
        } else {
          // 列表中没有，重新加载后再选中
          this.loadConversations().then(() => {
            const newConv = this.conversations.find(c => String(c.id) === String(conversationId));
            if (newConv) {
              this.selectConversation(newConv);
            } else {
              // 仍然找不到，直接用ID加载消息
              this.currentConv = { id: conversationId, name: '会话' };
              this.loadMessages(conversationId);
            }
          });
        }
      }
    },
    
    // --- Transaction Logic ---
    
    // 交换操作方法
    getExchangeStatusText(status) {
      const statusMap = {
        'pending': '待处理',
        'accepted': '已接受',
        'rejected': '已拒绝',
        'completed': '已完成',
        'cancelled': '已取消'
      };
      return statusMap[status] || '未知状态';
    },
    
    async handleAcceptExchange() {
      try {
        const response = await this.$axios.put(`/exchanges/${this.exchangeInfo.id}/accept`);
        if (response.code === 20000) {
          this.exchangeInfo.status = 'accepted';
          this.$message.success('已接受交换申请');
          // 发送系统消息
          this.sendSystemMessage('卖家已接受交换申请');
        }
      } catch (error) {
        console.error('接受交换失败:', error);
        this.$message.error('操作失败，请重试');
      }
    },
    
    async handleRejectExchange() {
      try {
        const response = await this.$axios.put(`/exchanges/${this.exchangeInfo.id}/reject`);
        if (response.code === 20000) {
          this.exchangeInfo.status = 'rejected';
          this.$message.success('已拒绝交换申请');
          this.sendSystemMessage('卖家拒绝了交换申请');
        }
      } catch (error) {
        console.error('拒绝交换失败:', error);
        this.$message.error('操作失败，请重试');
      }
    },
    
    async handleCompleteExchange() {
      try {
        const response = await this.$axios.put(`/exchanges/${this.exchangeInfo.id}/complete`);
        if (response.code === 20000) {
          this.exchangeInfo.status = 'completed';
          this.$message.success('交换已完成');
          this.sendSystemMessage('交换已完成');
        }
      } catch (error) {
        console.error('完成交换失败:', error);
        this.$message.error('操作失败，请重试');
      }
    },
    
    async handleCancelExchange() {
      try {
        const response = await this.$axios.put(`/exchanges/${this.exchangeInfo.id}/cancel`);
        if (response.code === 20000) {
          this.exchangeInfo.status = 'cancelled';
          this.$message.success('已取消交换申请');
          this.sendSystemMessage('买家取消了交换申请');
        }
      } catch (error) {
        console.error('取消交换失败:', error);
        this.$message.error('操作失败，请重试');
      }
    },
    
    sendSystemMessage(content) {
      // 发送系统消息到聊天
      this.messages.push({
        id: Date.now(),
        content: content,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        isSystem: true,
        type: 'text'
      });
      this.scrollToBottom();
    },
    
    checkClosedChats() {
      try {
        const stored = localStorage.getItem('closedChats');
        if (stored) {
          const closedChats = JSON.parse(stored);
          if (closedChats.length > 0) {
            // 过滤掉已关闭的群聊
            this.conversations = this.conversations.filter(c => !closedChats.includes(c.id));
            
            // 如果当前选中的会话被关闭，重置选中状态
            if (this.currentConv.id && closedChats.includes(this.currentConv.id)) {
              this.currentConv = {};
              this.messages = [];
              if (this.conversations.length > 0) {
                this.selectConversation(this.conversations[0]);
              }
            }
          }
        }
      } catch (e) {
        console.error('Failed to check closed chats', e);
      }
    },
    handleNewChat() {
      if (this.$route.query.init === 'true') {
        const dataStr = sessionStorage.getItem('pendingChatData');
        if (dataStr) {
          try {
            const data = JSON.parse(dataStr);
            const { id, name, type, avatar, members, goodsTitle, goodsStatus } = data;
            
            // 检查是否已存在
            const existing = this.conversations.find(c => c.id === id);
            if (existing) {
              this.selectConversation(existing);
            } else {
              // 创建新会话
              const newConv = {
                id,
                name,
                avatar,
                lastMessage: '系统消息：群聊已创建',
                lastTime: '刚刚',
                unread: 0,
                type: type || 'group',
                online: true,
                members: members || []
              };
              
              this.conversations.unshift(newConv);
              this.selectConversation(newConv);
              
              // 添加系统消息和商品卡片
              this.messages = [
                { id: Date.now(), content: '系统消息：交换群聊已创建，请文明交流', time: '刚刚', isSystem: true }
              ];
              
              if (goodsTitle) {
                this.messages.push({
                  id: Date.now() + 1,
                  type: 'goods',
                  isSystem: false,
                  data: {
                    title: goodsTitle,
                    status: goodsStatus || 'available'
                  }
                });
              }
            }
            // 清理
            sessionStorage.removeItem('pendingChatData');
          } catch (e) {
            console.error('Failed to parse chat data', e);
          }
        }
      }
    },
    handleConvImageError(conv) {
      // 图片加载失败时，用默认头像而不是清空（清空会导致显示图标）
      conv.avatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png';
    },
    handleImageError(e) {
      e.target.src = 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png'; // 默认头像
    },
    selectConversation(conv) {
      this.currentConv = conv;
      // 清除未读
      conv.unread = 0;
      // 加载该会话的消息
      this.loadMessages(conv.id);
      // 加载交换信息（如果有）
      this.loadExchangeInfo(conv.id);
    },
    sendMessage() {
      this.sendMessageToBackend();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesArea;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  },
  head() {
    return {
      title: '消息中心 - 校园换物平台'
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.chat-page {
  height: calc(100vh - 80px); // 减去头部高度
  padding: 24px;
  overflow: hidden;
}

.chat-container {
  @include glass-card;
  height: 100%;
  display: flex;
  overflow: hidden;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  box-shadow: var(--card-shadow);
}

// 左侧侧边栏
.chat-sidebar {
  width: 320px;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  
  .sidebar-header {
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--color-text-primary);
      margin: 0;
    }
    
    .new-chat-btn {
      background: rgba(255, 255, 255, 0.05);
      border: none;
      color: var(--color-text-secondary);
      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: var(--color-primary);
      }
    }
  }
  
  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }
    
    .conversation-item {
      display: flex;
      padding: 12px;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-bottom: 4px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }
      
      &.active {
        background: rgba(var(--color-primary-rgb), 0.1);
        border: 1px solid rgba(var(--color-primary-rgb), 0.1);
      }
      
      .avatar-wrapper {
        position: relative;
        width: 48px;
        height: 48px;
        margin-right: 12px;
        flex-shrink: 0;
        
        .default-group-avatar {
          width: 100%;
          height: 100%;
          border-radius: 50%;
          background: rgba(var(--color-primary-rgb), 0.2);
          display: flex;
          align-items: center;
          justify-content: center;
          
          i {
            font-size: 24px;
            color: var(--color-primary);
          }
        }
        
        img {
          width: 100%;
          height: 100%;
          border-radius: 50%;
          object-fit: cover;
        }
        
        .online-status {
          position: absolute;
          bottom: 2px;
          right: 2px;
          width: 10px;
          height: 10px;
          border-radius: 50%;
          background: var(--color-secondary-success);
          border: 2px solid var(--card-bg);
        }
      }
      
      .conv-content {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .conv-top {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;
          
          .conv-name {
            font-size: 15px;
            font-weight: 600;
            color: var(--color-text-primary);
          }
          
          .conv-time {
            font-size: 12px;
            color: var(--color-text-disabled);
          }
        }
        
        .conv-bottom {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .conv-msg {
            font-size: 13px;
            color: var(--color-text-secondary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            margin: 0;
            flex: 1;
            margin-right: 8px;
            
            .group-tag {
              font-size: 11px;
              color: var(--color-primary);
              margin-right: 2px;
            }
          }
          
          .unread-badge {
            background: var(--color-secondary-danger);
            color: white;
            font-size: 10px;
            padding: 0 6px;
            height: 16px;
            line-height: 16px;
            border-radius: 8px;
            min-width: 16px;
            text-align: center;
          }
        }
      }
    }
  }
}

// 右侧聊天主区域
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
  
  .chat-header {
    padding: 16px 24px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(var(--color-bg-surface-rgb), 0.5);
    
    .user-info {
      display: flex;
      flex-direction: column;
      
      .user-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--color-text-primary);
      }
      
      .user-status {
        font-size: 12px;
        color: var(--color-secondary-success);
        margin-top: 2px;
      }
    }
  }
  
  // 交换信息面板
  .exchange-panel {
    padding: 16px 24px;
    background: rgba(var(--color-bg-surface-rgb), 0.5);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    
    .exchange-goods-info {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      
      .goods-thumb {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        object-fit: cover;
      }
      
      .goods-detail {
        display: flex;
        flex-direction: column;
        gap: 4px;
        flex: 1;
        
        .goods-header {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .goods-name {
          font-size: 14px;
          font-weight: 500;
          color: var(--color-text-primary);
        }
        
        .role-badge {
          font-size: 11px;
          padding: 2px 8px;
          border-radius: 10px;
          font-weight: 600;
          background: rgba(var(--color-primary-rgb), 0.15);
          color: var(--color-primary);
          white-space: nowrap;
        }
        
        .exchange-status {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 4px;
          width: fit-content;
          
          &.pending {
            background: rgba(var(--color-secondary-warning-rgb), 0.2);
            color: var(--color-secondary-warning);
          }
          
          &.accepted {
            background: rgba(var(--color-secondary-info-rgb), 0.2);
            color: var(--color-secondary-info);
          }
          
          &.completed {
            background: rgba(var(--color-secondary-success-rgb), 0.2);
            color: var(--color-secondary-success);
          }
          
          &.rejected, &.cancelled {
            background: rgba(var(--color-text-disabled-rgb), 0.2);
            color: var(--color-text-disabled);
          }
        }
      }
    }
    
    .exchange-actions {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .status-text {
        font-size: 13px;
        color: var(--color-text-secondary);
      }
      
      .success-text {
        font-size: 13px;
        color: var(--color-secondary-success);
        font-weight: 600;
      }
      
      .error-text {
        font-size: 13px;
        color: var(--color-text-disabled);
      }
    }
  }
  
  .messages-area {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }
    
    .message-row {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      max-width: 80%;
      
      &.is-me {
        align-self: flex-end;
        flex-direction: row-reverse;
        
        .msg-bubble {
          background: var(--color-primary);
          color: white;
          border-radius: 16px 4px 16px 16px;
        }
        
        .msg-time { text-align: right; }
      }
      
      &:not(.is-me) {
        .msg-bubble {
          background: rgba(255, 255, 255, 0.1);
          color: var(--color-text-primary);
          border-radius: 4px 16px 16px 16px;
        }
      }
      
      .msg-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        object-fit: cover;
      }
      
      .msg-content {
        display: flex;
        flex-direction: column;
        gap: 4px;
        
        .msg-sender-name {
          font-size: 11px;
          color: var(--color-text-disabled);
          padding: 0 4px;
        }
        
        .msg-bubble {
          padding: 12px 16px;
          font-size: 14px;
          line-height: 1.5;
          word-break: break-all;
          
          &.image-bubble {
            padding: 4px;
            background: transparent;
            
            .chat-image {
              max-width: 300px;
              max-height: 300px;
              border-radius: 8px;
              cursor: pointer;
              transition: transform 0.2s;
              
              &:hover {
                transform: scale(1.02);
              }
            }
          }
          
          &.file-bubble {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            cursor: pointer;
            min-width: 200px;
            
            i {
              font-size: 32px;
              color: var(--color-primary);
            }
            
            .file-info {
              flex: 1;
              display: flex;
              flex-direction: column;
              gap: 4px;
              
              .file-name {
                font-size: 14px;
                font-weight: 500;
                color: var(--color-text-primary);
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              
              .file-size {
                font-size: 12px;
                color: var(--color-text-secondary);
              }
            }
            
            &:hover {
              opacity: 0.8;
            }
          }
          
          &.location-bubble {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            cursor: pointer;
            min-width: 200px;
            
            i {
              font-size: 32px;
              color: var(--color-secondary-danger);
            }
            
            .location-info {
              flex: 1;
              display: flex;
              flex-direction: column;
              gap: 4px;
              
              .location-address {
                font-size: 14px;
                font-weight: 500;
                color: var(--color-text-primary);
              }
              
              .location-tip {
                font-size: 12px;
                color: var(--color-text-secondary);
              }
            }
            
            &:hover {
              opacity: 0.8;
            }
          }
        }
        
        .msg-time {
          font-size: 12px;
          color: var(--color-text-disabled);
          margin: 0 4px;
        }
      }
    }

    &.is-system {
      justify-content: center;
      width: 100%;
      max-width: 100%;
      .system-msg {
        background: rgba(0, 0, 0, 0.2);
        color: var(--color-text-secondary);
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
      }
    }
    
    &.is-goods {
      justify-content: center;
      width: 100%;
      max-width: 100%;
      margin-bottom: 12px;
      
      .goods-bubble {
        background: #1E2A3A;
        border-radius: 16px;
        padding: 12px 20px;
        display: flex;
        align-items: center;
        gap: 16px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.05);
        max-width: 80%; // 限制最大宽度，防止撑满
        min-width: 200px; // 最小宽度
        
        .goods-header {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          flex-shrink: 0; // 防止头部被压缩
          
          .exchange-icon-circle {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: rgba(var(--color-primary-rgb), 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 4px;
            
            i {
              font-size: 18px;
              color: var(--color-primary);
            }
          }
          
          .exchange-label {
            font-size: 12px;
            color: rgba(255, 255, 255, 0.7);
          }
        }
        
        .goods-content {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1; // 占据剩余空间
          min-width: 0; // 允许文本截断
          
          .goods-title {
            font-size: 16px;
            color: #fff;
            font-weight: 500;
            white-space: nowrap; // 不换行
            overflow: hidden;
            text-overflow: ellipsis; // 超出显示省略号
          }
          
          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #10b981;
            box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
            flex-shrink: 0;
            
            &.exchanged { background: var(--color-text-disabled); box-shadow: none; }
          }
        }
      }
    }
  }
  
  .input-area {
    padding: 16px 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    background: rgba(var(--color-bg-surface-rgb), 0.3);
    
    .toolbar {
      display: flex;
      gap: 16px;
      margin-bottom: 12px;
      
      .tool-icon {
        font-size: 20px;
        color: var(--color-text-secondary);
        cursor: pointer;
        transition: color 0.2s;
        
        &:hover { color: var(--color-primary); }
      }
    }
    
    textarea {
      width: 100%;
      height: 80px;
      background: transparent;
      border: none;
      color: var(--color-text-primary);
      font-size: 14px;
      resize: none;
      outline: none;
      font-family: inherit;
      
      &::placeholder { color: var(--color-text-disabled); }
    }
    
    .send-bar {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 12px;
      
      .tip {
        font-size: 12px;
        color: var(--color-text-disabled);
      }
    }
  }
  
  .empty-chat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--color-text-disabled);
    
    i { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
    p { font-size: 16px; margin: 0; }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .chat-page {
    padding: 0;
    height: calc(100vh - 60px);
  }
  
  .chat-sidebar {
    width: 80px; // 缩小侧边栏
    
    .sidebar-header h3, .conv-content, .new-chat-btn { display: none; }
    .sidebar-header { justify-content: center; padding: 10px; }
    .avatar-wrapper { margin: 0; width: 40px; height: 40px; }
    .conversation-item { justify-content: center; padding: 10px; }
  }
}
</style>
