<template>
  <transition name="slide-in">
    <div v-if="isVisible" class="floating-chat" @click.stop>
      <div class="chat-header">
        <div class="header-left">
          <div class="bot-avatar">
            <i class="el-icon-service"></i>
          </div>
          <div class="header-info">
            <h3 class="bot-name">小O助手</h3>
            <div class="bot-status" :class="{ 'online': isOnline }">
              <span class="status-dot"></span>
              <span>{{ isOnline ? '在线' : '离线' }}</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <i class="el-icon-refresh action-btn" @click="loadHistory" title="刷新"></i>
          <i class="el-icon-delete action-btn" @click="clearHistory" title="清空"></i>
          <i class="el-icon-close action-btn" @click="closeChat" title="关闭"></i>
        </div>
      </div>

      <div class="chat-body" ref="chatBody">
        <div v-if="messages.length === 0" class="empty-state">
          <i class="el-icon-chat-dot-round"></i>
          <p>向小O助手发送消息</p>
        </div>

        <div v-else class="messages-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role"
          >
            <div class="message-avatar">
              <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-service'"></i>
            </div>
            <div class="message-bubble">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
            </div>
          </div>

          <div v-if="isTyping" class="message-item assistant typing">
            <div class="message-avatar">
              <i class="el-icon-service"></i>
            </div>
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

      <div class="chat-footer">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            placeholder="输入消息... (Enter发送)"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact.prevent="inputMessage += '\n'"
            :disabled="isSending || !isOnline"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isSending || !isOnline"
          >
            <i class="el-icon-s-promotion"></i>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'FloatingChat',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      messages: [],
      inputMessage: '',
      isSending: false,
      isTyping: false,
      isOnline: false,
      userId: '',
      baseUrl: 'http://106.52.174.132:3016'
    }
  },
  computed: {
    isVisible() {
      return this.visible
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initChat()
      }
    }
  },
  mounted() {
    if (this.visible) {
      this.initChat()
    }
  },
  methods: {
    async initChat() {
      const userInfo = this.$store.getters.userInfo
      this.userId = userInfo?.userId || userInfo?.id || 'admin'
      await this.checkHealth()
      if (this.isOnline) {
        await this.loadHistory()
      }
    },

    async checkHealth() {
      try {
        const response = await fetch(`${this.baseUrl}/health`)
        this.isOnline = response.ok
      } catch (error) {
        this.isOnline = false
      }
    },

    async loadHistory() {
      try {
        const response = await fetch(`${this.baseUrl}/history/${this.userId}`)
        if (response.ok) {
          const data = await response.json()
          this.messages = data.history || []
          this.$nextTick(() => this.scrollToBottom())
        }
      } catch (error) {
        console.error('加载历史失败:', error)
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
        const response = await fetch(`${this.baseUrl}/chat`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            message: userMessage,
            userId: this.userId
          })
        })

        if (response.ok) {
          const data = await response.json()
          this.messages.push({
            role: 'assistant',
            content: data.response,
            timestamp: new Date().toISOString()
          })
          this.$nextTick(() => this.scrollToBottom())
        }
      } catch (error) {
        console.error('发送失败:', error)
        this.messages.pop()
      } finally {
        this.isTyping = false
        this.isSending = false
      }
    },

    async clearHistory() {
      if (!confirm('确定清空所有对话？')) return

      try {
        const response = await fetch(`${this.baseUrl}/history/${this.userId}`, {
          method: 'DELETE'
        })
        if (response.ok) {
          this.messages = []
        }
      } catch (error) {
        console.error('清空失败:', error)
      }
    },

    closeChat() {
      this.$emit('close')
    },

    scrollToBottom() {
      const chatBody = this.$refs.chatBody
      if (chatBody) {
        chatBody.scrollTop = chatBody.scrollHeight
      }
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
      return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style lang="scss" scoped>
.floating-chat {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  width: 380px;
  height: 600px;
  background: rgba(20, 20, 40, 0.95);
  backdrop-filter: blur(30px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  z-index: 9999;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .bot-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea, #764ba2);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: white;
  }

  .header-info {
    .bot-name {
      font-size: 16px;
      font-weight: 600;
      color: white;
      margin: 0 0 4px 0;
    }

    .bot-status {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.6);

      .status-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: #999;
      }

      &.online .status-dot {
        background: #52c41a;
        box-shadow: 0 0 8px rgba(82, 196, 26, 0.8);
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 8px;

    .action-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      color: rgba(255, 255, 255, 0.7);
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
        color: white;
      }
    }
  }
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: rgba(255, 255, 255, 0.4);

    i {
      font-size: 48px;
      margin-bottom: 12px;
    }

    p {
      font-size: 14px;
    }
  }

  .messages-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .message-item {
    display: flex;
    gap: 8px;
    animation: fadeIn 0.3s ease;

    &.user {
      flex-direction: row-reverse;

      .message-bubble {
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 16px 16px 4px 16px;
      }

      .message-time {
        text-align: right;
      }
    }

    &.assistant {
      .message-bubble {
        background: rgba(255, 255, 255, 0.08);
        border-radius: 16px 16px 16px 4px;
      }
    }

    .message-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      i {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.7);
      }
    }

    .message-bubble {
      max-width: 70%;
      padding: 10px 14px;
      color: white;
    }

    .message-text {
      font-size: 14px;
      line-height: 1.5;
      word-wrap: break-word;
      white-space: pre-wrap;
    }

    .message-time {
      font-size: 11px;
      color: rgba(255, 255, 255, 0.5);
      margin-top: 4px;
    }

    &.typing .typing-dots {
      display: flex;
      gap: 4px;
      padding: 4px 0;

      span {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        animation: typing 1.4s infinite;

        &:nth-child(2) { animation-delay: 0.2s; }
        &:nth-child(3) { animation-delay: 0.4s; }
      }
    }
  }
}

.chat-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .input-wrapper {
    display: flex;
    gap: 8px;
    align-items: flex-end;

    textarea {
      flex: 1;
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      padding: 10px 12px;
      color: white;
      font-size: 14px;
      resize: none;
      max-height: 100px;
      font-family: inherit;

      &:focus {
        outline: none;
        border-color: #667eea;
        background: rgba(255, 255, 255, 0.12);
      }

      &::placeholder {
        color: rgba(255, 255, 255, 0.4);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .send-btn {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea, #764ba2);
      border: none;
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;
      flex-shrink: 0;

      i {
        font-size: 18px;
      }

      &:hover:not(:disabled) {
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}

.slide-in-enter-active, .slide-in-leave-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-in-enter, .slide-in-leave-to {
  transform: translateX(-100%) translateY(-50%);
  opacity: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}
</style>
