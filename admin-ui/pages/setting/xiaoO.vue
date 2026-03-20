<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-header">
        <div class="header-left">
          <div class="bot-avatar">
            <i class="el-icon-service"></i>
          </div>
          <div class="header-info">
            <h2 class="bot-name">小O助手</h2>
            <div class="bot-status" :class="{ 'online': isOnline }">
              <span class="status-dot"></span>
              <span>{{ isOnline ? '在线' : '离线' }}</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <button class="glass-btn btn-danger" @click="clearHistory">
            <i class="el-icon-delete"></i>
            <span>清空对话</span>
          </button>
        </div>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="messages.length === 0" class="empty-state">
          <i class="el-icon-chat-dot-round"></i>
          <p>还没有对话记录</p>
          <p class="hint">向小O助手发送消息开始对话吧！</p>
        </div>

        <div v-else class="messages-list">
          <template v-for="(msg, index) in messages">
            <!-- 时间戳分隔 -->
            <div
              v-if="shouldShowTimestamp(index)"
              :key="`time-${index}`"
              class="message-timestamp"
            >
              {{ formatTimestamp(msg.timestamp) }}
            </div>

            <!-- 消息内容 -->
            <div
              :key="index"
              class="message-item"
              :class="{ 'user-message': msg.role === 'user', 'bot-message': msg.role === 'assistant' }"
            >
              <div class="message-avatar">
                <i v-if="msg.role === 'assistant'" class="el-icon-service"></i>
                <img v-else-if="msg.role === 'user' && userAvatar" :src="userAvatar" alt="用户头像" />
                <i v-else class="el-icon-user"></i>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div class="message-text">{{ msg.content }}</div>
                </div>
              </div>
            </div>
          </template>

          <div v-if="isTyping" class="message-item bot-message typing-indicator">
            <div class="message-avatar">
              <i class="el-icon-service"></i>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <div class="typing-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-footer">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入消息..."
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact.prevent="inputMessage += '\n'"
            :disabled="isSending || !isOnline"
          ></el-input>
          <el-button
            type="primary"
            icon="el-icon-s-promotion"
            circle
            @click="sendMessage"
            :loading="isSending"
            :disabled="!inputMessage.trim() || !isOnline"
            class="send-button"
          ></el-button>
        </div>
        <div class="input-hint">
          按 Enter 发送，Shift + Enter 换行
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'XiaoOChat',
  data() {
    return {
      messages: [],
      inputMessage: '',
      isSending: false,
      isTyping: false,
      isOnline: false,
      loadingHistory: false,
      userId: '',
      baseUrl: '/kiro-api',
      keyName: 'claude-haiku', // 使用 Claude API Key
      model: 'claude-haiku-4-5-20251001' // 使用 Claude Haiku 模型
    }
  },
  computed: {
    userAvatar() {
      const userInfo = this.$store.getters.userInfo
      return userInfo?.avatar || ''
    }
  },
  mounted() {
    this.initChat()
  },
  methods: {
    async initChat() {
      const userInfo = this.$store.getters.userInfo
      this.userId = userInfo?.userId || userInfo?.id || 'admin'
      await this.checkHealth()
    },

    async checkHealth() {
      try {
        const response = await fetch(`${this.baseUrl}/keys/list`)
        this.isOnline = response.ok
      } catch (error) {
        this.isOnline = false
        this.$root.$emit('show-island-message', '无法连接到小O助手服务器', 'warning')
      }
    },

    async loadHistory() {
      this.loadingHistory = true
      try {
        const response = await fetch(`${this.baseUrl}/history/${this.userId}`)
        if (response.ok) {
          const result = await response.json()
          if (result.code === 200 && result.data && result.data.history) {
            // 转换 API 返回的格式为前端需要的格式
            this.messages = result.data.history.map(item => ({
              role: item.role,
              content: item.message,
              timestamp: item.timestamp
            }))
            this.$nextTick(() => this.scrollToBottom())
          }
        }
      } catch (error) {
        this.$root.$emit('show-island-message', '加载历史记录失败', 'danger')
      } finally {
        this.loadingHistory = false
      }
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.isSending || !this.isOnline) return

      const userMessage = this.inputMessage.trim()
      this.inputMessage = ''

      this.messages.push({
        role: 'user',
        content: userMessage,
        timestamp: new Date().toISOString()
      })

      this.$nextTick(() => this.scrollToBottom())

      this.isTyping = true
      this.isSending = true

      try {
        const response = await fetch(`${this.baseUrl}/proxy/chat`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            keyName: this.keyName,
            message: userMessage,
            model: this.model
          })
        })

        if (response.ok) {
          const result = await response.json()

          if (result.code === 200 && result.data) {
            // 兼容两种返回格式：reply 或 message
            const replyContent = result.data.reply || result.data.message

            if (replyContent) {
              // 添加助手回复到界面
              this.messages.push({
                role: 'assistant',
                content: replyContent,
                timestamp: new Date().toISOString()
              })
              this.$nextTick(() => this.scrollToBottom())
            } else {
              throw new Error('响应格式错误')
            }
          } else {
            throw new Error('响应格式错误')
          }
        } else {
          throw new Error('发送失败')
        }
      } catch (error) {
        this.$root.$emit('show-island-message', '发送消息失败', 'danger')
        this.messages.pop()
      } finally {
        this.isTyping = false
        this.isSending = false
      }
    },

    async clearHistory() {
      try {
        await this.$confirm('确定要清空所有对话记录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.messages = []
        this.$root.$emit('show-island-message', '对话记录已清空', 'success')
      } catch (error) {
        // 用户取消
      }
    },

    scrollToBottom() {
      const chatBody = this.$refs.chatBody
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight
      }
    },

    shouldShowTimestamp(index) {
      // 第一条消息总是显示时间戳
      if (index === 0) return true

      const currentMsg = this.messages[index]
      const prevMsg = this.messages[index - 1]

      if (!currentMsg.timestamp || !prevMsg.timestamp) return false

      const currentTime = new Date(currentMsg.timestamp).getTime()
      const prevTime = new Date(prevMsg.timestamp).getTime()

      // 如果间隔超过5分钟（300000毫秒），显示时间戳
      return (currentTime - prevTime) > 300000
    },

    formatTimestamp(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()

      // 今天
      if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }

      // 昨天
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      if (date.toDateString() === yesterday.toDateString()) {
        return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }

      // 其他日期
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
      return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.chat-page {
  height: calc(100vh - 180px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.chat-container {
  width: 100%;
  max-width: 1200px;
  height: 100%;
  @include glass-effect;
  border-radius: $border-radius-card;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-bottom: 1px solid $color-border;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .bot-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba($color-primary-rgb, 1), rgba($color-primary-rgb, 0.6));
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: white;
  }

  .header-info {
    .bot-name {
      font-size: 18px;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 4px 0;
    }

    .bot-status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: $color-text-secondary;

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #ccc;
      }

      &.online .status-dot {
        background: $color-secondary-success;
        box-shadow: 0 0 8px rgba($color-secondary-success-rgb, 0.6);
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.glass-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;

  i {
    font-size: 16px;
  }

  &.btn-secondary {
    background: rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.15);
    color: $color-text-primary;

    &:hover:not(:disabled) {
      background: rgba(255, 255, 255, 0.12);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }

    &:active {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  &.btn-danger {
    background: linear-gradient(135deg, rgba($color-secondary-danger-rgb, 0.9), rgba($color-secondary-danger-rgb, 0.8));
    border: 1px solid rgba($color-secondary-danger-rgb, 0.3);
    color: #fff;
    box-shadow: 0 4px 12px rgba($color-secondary-danger-rgb, 0.3);

    &:hover {
      background: linear-gradient(135deg, rgba($color-secondary-danger-rgb, 1), rgba($color-secondary-danger-rgb, 0.9));
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba($color-secondary-danger-rgb, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: $color-text-secondary;

    i {
      font-size: 64px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    p {
      margin: 4px 0;
      font-size: 16px;
    }

    .hint {
      font-size: 14px;
      opacity: 0.7;
    }
  }

  .messages-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .message-timestamp {
    text-align: center;
    font-size: 12px;
    color: $color-text-disabled;
    padding: 8px 0;
    margin: 4px 0;
  }

  .message-item {
    display: flex;
    gap: 12px;
    animation: fadeIn 0.3s ease;

    &.user-message {
      flex-direction: row-reverse;

      .message-bubble {
        background: linear-gradient(135deg, rgba($color-primary-rgb, 1), rgba($color-primary-rgb, 0.85));
        color: white;
        border-radius: 18px 18px 4px 18px;
      }
    }

    &.bot-message {
      .message-bubble {
        background: rgba(255, 255, 255, 0.08);
        backdrop-filter: blur(10px);
        color: $color-text-primary;
        border-radius: 4px 18px 18px 18px;
      }
    }

    .message-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      overflow: hidden;

      i {
        font-size: 18px;
        color: $color-text-secondary;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    &.bot-message .message-avatar {
      background: linear-gradient(135deg, rgba($color-primary-rgb, 0.3), rgba($color-primary-rgb, 0.2));

      i {
        color: rgba($color-primary-rgb, 1);
      }
    }

    &.user-message .message-avatar {
      background: rgba(255, 255, 255, 0.08);
    }

    .message-content {
      max-width: 70%;
    }

    .message-bubble {
      padding: 10px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      max-width: fit-content;
    }

    .message-text {
      font-size: 15px;
      line-height: 1.5;
      word-wrap: break-word;
      white-space: pre-wrap;
    }
  }

  .typing-indicator {
    .typing-dots {
      display: flex;
      gap: 4px;
      padding: 8px 0;

      span {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: $color-text-secondary;
        animation: typing 1.4s infinite;

        &:nth-child(2) {
          animation-delay: 0.2s;
        }

        &:nth-child(3) {
          animation-delay: 0.4s;
        }
      }
    }
  }
}

.chat-footer {
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-top: 1px solid $color-border;

  .input-container {
    display: flex;
    gap: 12px;
    align-items: flex-end;

    ::v-deep .el-textarea {
      flex: 1;

      .el-textarea__inner {
        background: rgba(255, 255, 255, 0.08);
        border: 1px solid $color-border;
        color: $color-text-primary;
        border-radius: 12px;
        padding: 12px 16px;

        &:focus {
          border-color: $color-primary;
          background: rgba(255, 255, 255, 0.12);
        }

        &::placeholder {
          color: $color-text-disabled;
        }
      }
    }

    .send-button {
      width: 40px;
      height: 40px;
    }
  }

  .input-hint {
    font-size: 12px;
    color: $color-text-disabled;
    margin-top: 8px;
    text-align: center;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}
</style>
